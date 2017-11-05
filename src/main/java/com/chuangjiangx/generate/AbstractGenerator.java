package com.chuangjiangx.generate;

import com.chuangjiangx.model.ClassComment;
import com.chuangjiangx.util.ContextUtil;

import lombok.extern.slf4j.Slf4j;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.List;

/**
 * @author by zhoutao on 2017/11/2.
 */
@Slf4j
public abstract class AbstractGenerator implements Generator {

    protected abstract StringBuilder generateStringBuilder(ClassComment classComment);

    @Override
    public void generate(List<ClassComment> classComments) {
        for (ClassComment classComment : classComments) {
            File filePath = new File((String) ContextUtil.get(ContextUtil.OUTPUT_KEY));
            if (!filePath.exists()) {
                filePath.mkdirs();
            }
            File mdFile = new File(filePath, classComment.getClassName() + ".md");
            StringBuilder sb = generateStringBuilder(classComment);
            try (FileOutputStream fos = new FileOutputStream(mdFile);
                 BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos))
            ) {
                bw.write(sb.toString());
                String command = "";
                if (System.getProperty("os.name").startsWith("W")) {
                    command = "CMD /c ";
                }
                Runtime.getRuntime().exec(command + "aglio -i "
                        + mdFile.getAbsolutePath() + " -o " + mdFile.getParentFile().getAbsolutePath()
                        + "/" + classComment.getClassName() + ".html");
            } catch (Exception ex) {
                log.error("解析生成MD文件异常");
            }
        }
    }
}
