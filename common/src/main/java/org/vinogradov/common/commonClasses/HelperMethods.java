package org.vinogradov.common.commonClasses;


import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class HelperMethods {

    public static String delSpace(String str) {
        String newStr = str.trim();
        newStr = newStr.replaceAll(" ", "");
        return newStr;
    }

    public static List<FileInfo> generateFileInfoList(Path path) {
        try {
            return Files.list(path).map(FileInfo::new).collect(Collectors.toList());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static Map<String, String> creatDstPaths(Path srcPathDirectory, Path dstPathDirectory) {
        Map<String, String> mapPathsSrcDst = new HashMap<>();
        Consumer<Path> createDstPathConsumer = (srcPathEntryFile) -> {
            String newDstPathFile = createNewDstPath(srcPathDirectory, srcPathEntryFile, dstPathDirectory);
            mapPathsSrcDst.put(srcPathEntryFile.toString(), newDstPathFile);
        };
        filesWalk(srcPathDirectory, createDstPathConsumer);
        return mapPathsSrcDst;
    }

    public static boolean splitFile(Long id, String path, MyFunction<Long, byte[], Boolean> filePartMyFunction) {
        byte[] filePart = new byte[Constants.MB_1];
        try (FileInputStream fileInputStream = new FileInputStream(path)) {
            int size;
            while ((size = fileInputStream.read(filePart)) != -1) {
                if (size < filePart.length) {
                    filePart = getNewByteArr(filePart, size);
                } else {
                    filePart = getNewByteArr(filePart, filePart.length);
                }
                if (filePartMyFunction.apply(id, filePart)) {
                    return true;
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return false;
    }

    public static FileOutputStream generateFileOutputStream(String path) {
        FileOutputStream fileOutputStream;
        try {
            fileOutputStream = new FileOutputStream(path);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        return fileOutputStream;
    }

    //реализация паттерна "Visitor" - метод filesWalk() принемает на вход
    //директорию с файлами, затем осуществляет проход по всем файлам с применением
    //к ним логики pathConsumer
    private static void filesWalk(Path directory, Consumer<Path> pathConsumer) {
        try {
            List<Path> list = Files.list(directory).collect(Collectors.toList());
            for (Path filePathEntry : list) {
                if (Files.isDirectory(filePathEntry)) {
                    filesWalk(filePathEntry, pathConsumer);
                } else {
                    pathConsumer.accept(filePathEntry);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static String createNewDstPath(Path srcPathDirectory, Path srcPathEntryFile, Path dstPathDirectory) {
        String pathSrc = srcPathDirectory.toString();
        String pathSrcEntryFile = srcPathEntryFile.toString();
        String pathDst = dstPathDirectory.toString();
        String partString = pathSrcEntryFile.replace(pathSrc, "");
        String newPath = pathDst.concat(partString);
        return newPath;
    }

    public static void createNewDirectoryRecursion(Path pathParentFile) {
        Path pathChild = null;
        if (!Files.exists(pathParentFile)) {
            pathChild = pathParentFile;
            createNewDirectoryRecursion(pathParentFile.getParent());
        }
        try {
            if (pathChild != null) {
                Files.createDirectory(pathChild);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean deleteUserFile(Path srcPath) {
        try (Stream<Path> walk = Files.walk(srcPath)) {
            walk.sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
            System.out.println("Удаленный файл или папка: " + srcPath);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static void createNewUserDirectory(Path srcPath) {
        if (!srcPath.toFile().exists()) {
            try {
                Files.createDirectory(srcPath);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static long sumSizeFiles(Path directory) {
        long sumSize = 0;
        List<Long> sizeList = new ArrayList<>();
        Consumer<Path> sumSizeFile = (path) -> {
            try {
                Long size = Files.size(path);
                sizeList.add(size);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        };

        filesWalk(directory, sumSizeFile);
        for (Long sizeFile : sizeList) {
            sumSize += sizeFile;
        }
        return sumSize;
    }

    private static byte[] getNewByteArr(byte[] filePart, int size) {
        byte[] newArr = new byte[size];
        System.arraycopy(filePart, 0, newArr, 0, size);
        return newArr;
    }


}
