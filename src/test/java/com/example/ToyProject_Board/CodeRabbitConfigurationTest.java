package com.example.ToyProject_Board;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;

class CodeRabbitConfigurationTest {

	private static final Path CONFIG_PATH = Path.of(".coderabbit.yaml");

	private static Map<String, Object> configuration;

	@BeforeAll
	static void CodeRabbit_설정_읽기() throws IOException {
		assertThat(CONFIG_PATH).exists().isRegularFile();

		LoaderOptions loaderOptions = new LoaderOptions();
		loaderOptions.setAllowDuplicateKeys(false);

		try (InputStream inputStream = Files.newInputStream(CONFIG_PATH)) {
			Object parsedConfiguration = new Yaml(new SafeConstructor(loaderOptions)).load(inputStream);
			configuration = stringKeyedMap(parsedConfiguration, "최상위");
		}
	}

	@Test
	@DisplayName("리뷰 언어와 기본 리뷰 설정을 유지한다")
	void 리뷰_기본_설정_검증() {
		assertThat(configuration).containsEntry("language", "ko-KR");

		Map<String, Object> reviews = mapValue(configuration, "reviews");
		assertThat(reviews)
				.containsEntry("profile", "chill")
				.containsEntry("request_changes_workflow", false)
				.containsEntry("high_level_summary", true)
				.containsEntry("review_status", true)
				.containsEntry("collapse_walkthrough", false)
				.containsEntry("poem", false);
	}

	@Test
	@DisplayName("main과 develop 대상의 비초안 PR을 자동 리뷰한다")
	void 자동_리뷰_대상_검증() {
		Map<String, Object> autoReview = mapValue(mapValue(configuration, "reviews"), "auto_review");

		assertThat(autoReview)
				.containsEntry("enabled", true)
				.containsEntry("drafts", false);
		assertThat(stringListValue(autoReview, "base_branches"))
				.contains("main", "develop");
	}

	@Test
	@DisplayName("빌드 결과와 도구 생성 파일을 리뷰에서 제외한다")
	void 리뷰_제외_경로_검증() {
		List<String> pathFilters = stringListValue(mapValue(configuration, "reviews"), "path_filters");

		assertThat(pathFilters)
				.contains("!build/**", "!gradle/wrapper/**", "!**/.DS_Store");
	}

	@Test
	@DisplayName("경로 필터가 제외 규칙으로만 구성된다")
	void 경로_필터는_포함_규칙을_사용하지_않음() {
		List<String> pathFilters = stringListValue(mapValue(configuration, "reviews"), "path_filters");

		assertThat(pathFilters)
				.isNotEmpty()
				.allMatch(pathFilter -> pathFilter.startsWith("!"));
	}

	private static Map<String, Object> mapValue(Map<String, Object> parent, String key) {
		Object value = parent.get(key);
		return stringKeyedMap(value, "'%s' 설정".formatted(key));
	}

	private static List<String> stringListValue(Map<String, Object> parent, String key) {
		Object value = parent.get(key);
		assertThat(value)
				.as("'%s' 설정", key)
				.isInstanceOf(List.class);

		List<?> values = (List<?>) value;
		assertThat(values)
				.as("'%s' 설정 항목", key)
				.allSatisfy(item -> assertThat(item).isInstanceOf(String.class));
		return values.stream()
				.map(String.class::cast)
				.toList();
	}

	@SuppressWarnings("unchecked")
	private static Map<String, Object> stringKeyedMap(Object value, String description) {
		assertThat(value)
				.as(description)
				.isInstanceOf(Map.class);

		Map<?, ?> map = (Map<?, ?>) value;
		assertThat(map.keySet())
				.as("%s 키".formatted(description))
				.allSatisfy(key -> assertThat(key).isInstanceOf(String.class));
		return (Map<String, Object>) map;
	}
}
