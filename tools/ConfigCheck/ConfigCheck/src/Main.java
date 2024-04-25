import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class Main {
    public static void main(String[] args) {

        String currentWorkingDirectory = System.getProperty("user.dir");

        // 查詢條件路徑
        String serchPath = currentWorkingDirectory + "\\search\\searchFile.properties";

        //宣告使用者List
        ArrayList<String> userInputList = new ArrayList<String>();

        try {
            // 使用 Paths 获取文件路径
            Path path = Paths.get(serchPath);

            // 使用 Files 读取文件内容
            List<String> lines = Files.readAllLines(path);

            //
            for (String line : lines) {
                userInputList.add(line);
            }

            // 查詢內容文件夹路径
            String directoryPath = currentWorkingDirectory + "\\searchFile";

            // 使用 Paths 获取文件夹路径
            Path directory = Paths.get(directoryPath);

            // 將查詢結果放入ArrayList
            ArrayList<String> outList = processAllFilesInDirectory(directory, userInputList);

            // 輸出檔案路徑
            String outfile = currentWorkingDirectory + "\\result\\";

            // 获取当前日期和时间
            LocalDateTime currentTime = LocalDateTime.now();

            // 定义日期时间格式
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

            // 将日期时间格式化为字符串
            String formattedTime = currentTime.format(formatter);

            String fileName = formattedTime + ".txt";

            //輸出結果
            appendArrayListToFile(outList, outfile, fileName);

            System.out.println("成功");

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * 給入路徑及關鍵字 返回ArrayList
     *
     * @param directory
     * @param userInputList
     * @return
     * @throws IOException
     */
    private static ArrayList<String> processAllFilesInDirectory(Path directory, ArrayList<String> userInputList) throws IOException {

        ArrayList<String> outList = new ArrayList<String>();

        // 使用 Files.walk() 遍历文件夹下的所有文件
        Files.walkFileTree(directory, EnumSet.noneOf(FileVisitOption.class), Integer.MAX_VALUE, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {

                if (Files.isRegularFile(file) && !file.toString().endsWith(".p12") && !file.toString().endsWith(".cer")) {

                    // 读取文件内容
                    List<String> lines = Files.readAllLines(file);

                    // 输出文件内容，每一行前添加行号
                    outList.add("文件：" + file);

                    // 是否有符合
                    boolean check = true;

                    for (int i = 0; i < lines.size(); i++) { //遍歷查詢條件
                        for (String element : userInputList) {
                            if (lines.get(i).toLowerCase().contains(element.toLowerCase())) {
                                String lineNumbered = (i + 1) + ": " + lines.get(i) + "       使用關鍵字 : " + element;
                                outList.add(lineNumbered);
                                check = false;
                            }
                        }

                    }

                    if (check) {
                        // 如果沒找到1比符合
                        outList.add("檢核正常");
                    }

                }

                outList.add("");

                return FileVisitResult.CONTINUE;

            }

        });

        return outList;
    }

    /**
     * 給內容 路徑 檔名 將結果輸出
     *
     * @param arrayList
     * @param directoryPath
     * @param fileName
     */
    private static void appendArrayListToFile(ArrayList<String> arrayList, String directoryPath, String fileName) {
        try {
            // 构建完整的文件路径
            String filePath = directoryPath + fileName;

            // 创建 FileWriter 对象，追加模式
            FileWriter fileWriter = new FileWriter(filePath, true);

            // 创建 BufferedWriter 对象，用于写入文件
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);

            // 遍历 ArrayList，将内容写入文件
            for (String element : arrayList) {
                bufferedWriter.write(element);
                bufferedWriter.newLine(); // 换行
            }

            // 关闭 BufferedWriter 和 FileWriter 对象
            bufferedWriter.close();
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}