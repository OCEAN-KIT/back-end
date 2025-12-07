package com.ocean.piuda.environment;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ProjectExporter {

    // 결과 파일명
    private static final String OUTPUT_FILE = "project_context.txt";

    // 기본적으로 제외할 디렉토리/파일 (gitignore가 없거나 놓칠 경우 대비)
    private static final Set<String> DEFAULT_IGRORES = new HashSet<>(Arrays.asList(
            ".git", ".idea", ".gradle", "build", "target", "out", ".mvn", "wrapper", "gradlew", "gradlew.bat", OUTPUT_FILE
    ));

    // 텍스트 파일로 간주할 확장자 (바이너리 제외용)
    private static final Set<String> TEXT_EXTENSIONS = new HashSet<>(Arrays.asList(
            "java", "xml", "yml", "yaml", "properties", "gradle", "sql", "md", "txt", "html", "css", "js", "json", "dockerfile"
    ));

    public static void main(String[] args) {
        Path startPath = Paths.get(".");
        Path outputPath = Paths.get(OUTPUT_FILE);
        List<PathMatcher> gitIgnoreMatchers = loadGitIgnore(startPath);

        try (java.io.BufferedWriter writer = Files.newBufferedWriter(outputPath, StandardCharsets.UTF_8)) {
            System.out.println(" 프로젝트 추출을 시작합니다... (Target: " + OUTPUT_FILE + ")");

            Files.walkFileTree(startPath, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
                    if (shouldIgnore(startPath, dir, gitIgnoreMatchers)) {
                        return FileVisitResult.SKIP_SUBTREE;
                    }
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                    if (shouldIgnore(startPath, file, gitIgnoreMatchers)) {
                        return FileVisitResult.CONTINUE;
                    }

                    if (isTextFile(file)) {
                        writeFileContent(writer, file);
                    }
                    return FileVisitResult.CONTINUE;
                }
            });

            System.out.println("완료! '" + OUTPUT_FILE + "' 파일이 생성되었습니다.");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // .gitignore 파일 로드 및 PathMatcher 변환
    private static List<PathMatcher> loadGitIgnore(Path rootPath) {
        List<PathMatcher> matchers = new ArrayList<>();
        Path gitIgnorePath = rootPath.resolve(".gitignore");
        FileSystem fs = FileSystems.getDefault();

        if (Files.exists(gitIgnorePath)) {
            try (Stream<String> lines = Files.lines(gitIgnorePath)) {
                lines.map(String::trim)
                        .filter(line -> !line.isEmpty() && !line.startsWith("#"))
                        .forEach(line -> {
                            // 간단한 glob 패턴 변환 (폴더인 경우 /** 추가 등)
                            String pattern = "glob:**/" + line;
                            if (line.endsWith("/")) {
                                pattern += "**";
                            }
                            try {
                                matchers.add(fs.getPathMatcher(pattern));
                            } catch (Exception e) {
                                // 복잡한 정규식 패턴은 무시
                            }
                        });
                System.out.println("ℹ .gitignore 파일을 발견하여 규칙을 적용합니다.");
            } catch (IOException e) {
                System.err.println("⚠ .gitignore 읽기 실패: " + e.getMessage());
            }
        }
        return matchers;
    }

    // 제외 대상 확인 로직
    private static boolean shouldIgnore(Path root, Path path, List<PathMatcher> gitIgnoreMatchers) {
        String fileName = path.getFileName().toString();

        // 1. 기본 제외 목록 확인
        if (DEFAULT_IGRORES.contains(fileName)) return true;

        // 2. .gitignore 규칙 확인
        for (PathMatcher matcher : gitIgnoreMatchers) {
            if (matcher.matches(path)) return true;
        }

        return false;
    }

    // 텍스트 파일 여부 확인 (확장자 기반)
    private static boolean isTextFile(Path path) {
        String fileName = path.getFileName().toString().toLowerCase();
        if (fileName.equals("dockerfile")) return true;

        int lastDot = fileName.lastIndexOf('.');
        if (lastDot == -1) return false;

        String ext = fileName.substring(lastDot + 1);
        return TEXT_EXTENSIONS.contains(ext);
    }

    private static void writeFileContent(java.io.BufferedWriter writer, Path file) {
        try {
            writer.write("\n" + "=".repeat(80) + "\n");
            writer.write("File Path: " + file.toString() + "\n");
            writer.write("=".repeat(80) + "\n\n");

            // 파일 내용을 한 번에 읽어서 쓰기
            Files.lines(file).forEach(line -> {
                try {
                    writer.write(line);
                    writer.newLine();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
            writer.write("\n");
        } catch (Exception e) {
            System.err.println(" 파일 읽기 실패 (" + file + "): " + e.getMessage());
        }
    }
}