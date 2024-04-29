package hr.adinfo.client.print;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.BarcodeQRCode;
import com.itextpdf.text.pdf.ColumnText;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;
import com.itextpdf.text.pdf.PdfWriter;
//import hr.adinfo.client.AppTecaj;
import hr.adinfo.client.ClientAppLogger;
import hr.adinfo.client.ClientAppSettings;
import hr.adinfo.client.ClientAppUtils;
import hr.adinfo.client.datastructures.Invoice;
import hr.adinfo.client.datastructures.InvoiceTaxes;
import hr.adinfo.client.datastructures.PackagingRefunds;
import hr.adinfo.utils.Values;
import java.awt.Desktop;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.print.Doc;
import javax.print.DocFlavor;
import javax.print.DocPrintJob;
import javax.print.PrintServiceLookup;
import javax.print.SimpleDoc;
import javax.print.attribute.AttributeSet;
import javax.print.attribute.HashPrintServiceAttributeSet;
import javax.print.attribute.standard.PrinterName;
import javax.swing.JTable;
import javax.swing.table.TableModel;

/**
 *
 * @author Matej
 */
public class PrintUtils {
	
	// Header and footer
	private static int headerFooterSpace = 14;
	private static Values.AppSettingsEnum[] headerValuesA4 = new Values.AppSettingsEnum[]{
		Values.AppSettingsEnum.SETTINGS_HEADER_A4_1, Values.AppSettingsEnum.SETTINGS_HEADER_A4_2, Values.AppSettingsEnum.SETTINGS_HEADER_A4_3,
		Values.AppSettingsEnum.SETTINGS_HEADER_A4_4, Values.AppSettingsEnum.SETTINGS_HEADER_A4_5, Values.AppSettingsEnum.SETTINGS_HEADER_A4_6,
		Values.AppSettingsEnum.SETTINGS_HEADER_A4_7
	};
	private static Values.AppSettingsEnum[] footerValuesA4 = new Values.AppSettingsEnum[]{
		Values.AppSettingsEnum.SETTINGS_FOOTER_A4_1, Values.AppSettingsEnum.SETTINGS_FOOTER_A4_2, Values.AppSettingsEnum.SETTINGS_FOOTER_A4_3
	};
	private static Values.AppSettingsEnum[] headerValuesPos = new Values.AppSettingsEnum[]{
		Values.AppSettingsEnum.SETTINGS_HEADER_POS_1, Values.AppSettingsEnum.SETTINGS_HEADER_POS_2, Values.AppSettingsEnum.SETTINGS_HEADER_POS_3,
		Values.AppSettingsEnum.SETTINGS_HEADER_POS_4, Values.AppSettingsEnum.SETTINGS_HEADER_POS_5, Values.AppSettingsEnum.SETTINGS_HEADER_POS_6,
		Values.AppSettingsEnum.SETTINGS_HEADER_POS_7
	};
	private static Values.AppSettingsEnum[] footerValuesPos = new Values.AppSettingsEnum[]{
		Values.AppSettingsEnum.SETTINGS_FOOTER_POS_1, Values.AppSettingsEnum.SETTINGS_FOOTER_POS_2, Values.AppSettingsEnum.SETTINGS_FOOTER_POS_3
	};
	
	// Fonts
	private static final String defaultFont = FontFactory.TIMES_ROMAN;
	private static Font headerFooterFont;
    private static Font footerFont;
	private static Font regularFont;
	private static Font regularFontBold;
	private static Font titleFont;
	private static Font cellFont;
	private static Font cellFontBold;
	        
	// Alignment
	public static final String ALIGN_RIGHT_SUBSTRING = "%RRR%";
        
        private static float ExchangeRate = 7.53450f;
        public static String strExchangeRate = "7.53450";
	//private AppTecaj tecaj = new AppTecaj();
	public static void ClearPrintFolder(){
		String filePath = Paths.get("").toAbsolutePath() + File.separator + "PDF";
		DeleteDirectory(new File(filePath));
	}
	private static void DeleteDirectory(File file) {
		File[] contents = file.listFiles();
		if (contents != null) {
			for (File f : contents) {
				if (!Files.isSymbolicLink(f.toPath())) {
					DeleteDirectory(f);
				}
			}
		}
		file.delete();
	}
        
        private static int CheckDate(Date dtRacuna){
            int Rez = 0; //Rezultat moze bit 0-ne prolazi ni jedan uvjet ili je dobio krivi datum
                         //10 - datum je manji od 01.01.2023
                         //20 - datum je veći od 01.01.2023 a manji od 01.01.2024
                         //30 - datum je veći od 01.01.2024
            Date dtKonverzije = null;
            Date dtPrestanakDvojnogIskazivanja = null;
            try{
                String strDtKonverzije = "01.01.2023 0:0:0.000";
                String strDtKrajDvojnogIskazivanja = "01.01.2024 0:0:0.000";
                dtKonverzije = (new SimpleDateFormat("dd.MM.yyyy HH:mm:ss.SSS")).parse(strDtKonverzije);
                dtPrestanakDvojnogIskazivanja = (new SimpleDateFormat("dd.MM.yyyy HH:mm:ss.SSS")).parse(strDtKrajDvojnogIskazivanja);
            }catch(Exception ex){
                ex.printStackTrace();
            }
            if(dtRacuna.compareTo(dtKonverzije) < 0){
                Rez = 10;
            }else if((dtRacuna.compareTo(dtKonverzije)) > 0 && (dtRacuna.compareTo(dtPrestanakDvojnogIskazivanja) < 0)){
                Rez = 20;
            }else{
                Rez = 30;
            }
            return Rez;
        }
	
	public static void PrintA4Table(String documentName, String tableTitle, JTable jTable, String itemNote){
		int[][] columnIndex = new int[1][jTable.getColumnCount()];
		for(int i = 0; i < jTable.getColumnCount(); ++i){
			columnIndex[0][i] = i;
		}
		PrintA4Table(documentName, new String[] {tableTitle}, new JTable[]{jTable}, columnIndex, new int[][]{new int[]{}}, null, new boolean[]{false}, itemNote);
	}
	
	public static void PrintA4Table(String documentName, String tableTitle, JTable jTable, int[] columnIndex, int[] mergeIndex, PrintTableExtraData extraData, String itemNote){
		PrintA4Table(documentName, new String[] {tableTitle}, new JTable[]{jTable}, new int[][]{columnIndex}, new int[][]{mergeIndex}, new PrintTableExtraData[]{extraData}, new boolean[]{false}, itemNote);
	}
	
	public static void PrintA4Table(String documentName, String[] tableTitle, JTable[] jTable, int[][] columnIndex, int[][] mergeIndex, PrintTableExtraData[] extraData, boolean[] newTableNewPage, String itemNote){
		ClientAppSettings.LoadSettings();
		String filePath = Paths.get("").toAbsolutePath() + File.separator + "PDF";
		new File(filePath).mkdirs();
		String timestamp = new SimpleDateFormat("yyyyMMdd").format(new Date()) + "-" + new SimpleDateFormat("HHmmss").format(new Date());
		filePath = filePath + File.separator + documentName + "-" + timestamp + ".pdf";
		String tempFilePath = filePath + "temp.pdf";
		
		InitFonts();
		
		// Get header and footer size
		int headerLinesCount = GetHeaderLinesCountA4();
		int footerLinesCount = GetFooterLinesCountA4();
		
		// Margins - left, right, top, bottom
		Document document = new Document(PageSize.A4, 36f, 36f, 36f + headerFooterSpace + headerLinesCount * headerFooterSpace, 36f + 36f + headerFooterSpace + footerLinesCount * headerFooterSpace);
		try {
			PdfWriter.getInstance(document, new FileOutputStream(tempFilePath));
			document.open();
			
			for(int tableIndex = 0; tableIndex < tableTitle.length; ++tableIndex){
				TableModel tableModel = jTable[tableIndex].getModel();
				//document.add(Chunk.NEWLINE);
				//document.add(Chunk.NEWLINE);
				if(!"".equals(tableTitle[tableIndex])){
					document.add(new Paragraph(tableTitle[tableIndex], titleFont));
				}
				//document.add(Chunk.NEWLINE);

				if(extraData != null && extraData[tableIndex] != null){
					for (int i = 0; i < extraData[tableIndex].headerList.size(); ++i){
						AddTwoChunkLine(extraData[tableIndex].headerList.get(i).getKey(), extraData[tableIndex].headerList.get(i).getValue(), document);
					}
					document.add(Chunk.NEWLINE);
				}

				// Table
				PdfPTable pdfTable = new PdfPTable(columnIndex[tableIndex].length);
				pdfTable.getDefaultCell().setMinimumHeight(Values.PRINT_A4_TABLE_ROW_HEIGHT);
				pdfTable.setWidthPercentage(90);

				float[] columnWidths = new float[columnIndex[tableIndex].length];
				for (int i = 0; i < columnIndex[tableIndex].length; ++i){
					columnWidths[i] = jTable[tableIndex].getColumnModel().getColumn(columnIndex[tableIndex][i]).getPreferredWidth();
				}
				pdfTable.setWidths(columnWidths);

				for (int i = 0; i < columnIndex[tableIndex].length; ++i){
					PdfPCell header = new PdfPCell();
					//header.setBackgroundColor(new BaseColor(220, 240, 255));
					header.setPhrase(new Phrase(tableModel.getColumnName(columnIndex[tableIndex][i]), cellFontBold));
					header.setMinimumHeight(Values.PRINT_A4_TABLE_ROW_HEIGHT);
					header.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
					pdfTable.addCell(header);
				}

				for (int i = 0; i < jTable[tableIndex].getRowCount(); ++i){
					for (int j = 0; j < columnIndex[tableIndex].length; ++j){
						int rowSpan = 1;
						String cellValue = String.valueOf(tableModel.getValueAt(jTable[tableIndex].convertRowIndexToModel(i), columnIndex[tableIndex][j]));
						if("null".equals(cellValue)){
							cellValue = "";
						}
						if(ClientAppUtils.ArrayContains(mergeIndex[tableIndex], columnIndex[tableIndex][j])){
							// Check if cell should be skipped
							if(i > 0){
								String cellValuePrevious = String.valueOf(tableModel.getValueAt(jTable[tableIndex].convertRowIndexToModel(i-1), columnIndex[tableIndex][j]));
								if(cellValuePrevious.equals(cellValue)){
									continue;
								}
							}

							for(int k = i + 1; k < jTable[tableIndex].getRowCount(); k++){
								String cellValueNext = String.valueOf(tableModel.getValueAt(jTable[tableIndex].convertRowIndexToModel(k), columnIndex[tableIndex][j]));
								if(cellValueNext.equals(cellValue)){
									rowSpan++;
								} else {
									break;
								}
							}
						}

						PdfPCell cell = new PdfPCell(new Phrase(cellValue, cellFont));
						cell.setRowspan(rowSpan);
						cell.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
						cell.setMinimumHeight(Values.PRINT_A4_TABLE_ROW_HEIGHT);
						if(rowSpan != 1){
							cell.setVerticalAlignment(PdfPCell.ALIGN_MIDDLE);
						}
						pdfTable.addCell(cell);
					}
				}

				document.add(pdfTable);

				if(extraData != null && extraData[tableIndex] != null){
					document.add(Chunk.NEWLINE);
					for (int i = 0; i < extraData[tableIndex].footerList.size(); ++i) {
						//AddTwoChunkLine(extraData[tableIndex].footerList.get(i).getKey(), extraData[tableIndex].footerList.get(i).getValue(), document);

						if (extraData[tableIndex].footerList.get(i).getKey() == "ZKI:  ") {
                                                    String zki = extraData[tableIndex].footerList.get(i).getValue();
                                                    if(i+2 < extraData[tableIndex].footerList.size()){
                                                        String jir = extraData[tableIndex].footerList.get(i+1).getValue();
                                                        String qr = extraData[tableIndex].footerList.get(i+2).getValue();
                                                        if(!qr.equals("QR")){
                                                            try {                                                            
                                                                AddThreeChunkLine(zki, "ZKI:  ", qr, "JIR:  ", jir, document);
                                                                i = i + 2;
                                                            } catch (IOException ioex) {}
                                                        }else{
                                                            AddTwoChunkLine(extraData[tableIndex].footerList.get(i).getKey(), extraData[tableIndex].footerList.get(i).getValue(), document);
                                                        }
                                                    }                                                                                                        							
						} else if(extraData[tableIndex].footerList.get(i).getKey() == "QR"){continue;}
                                                else {
							AddTwoChunkLine(extraData[tableIndex].footerList.get(i).getKey(), extraData[tableIndex].footerList.get(i).getValue(), document);
						}
					}
				}
				
				if(newTableNewPage[tableIndex] && tableIndex != tableTitle.length - 1){
					document.newPage();
				}
			}
		} catch (FileNotFoundException | DocumentException ex) {
			ClientAppLogger.GetInstance().ShowMessage("Došlo je do pogreške tijekom generiranja dokumenta");
			return;
		}

		document.close();
		
		PrintA4HeaderFooterPages(tempFilePath, filePath);
		
		try {
			Files.deleteIfExists(Paths.get(tempFilePath));
		} catch (IOException ex) {}
		
		try {
			Desktop.getDesktop().open(new File(filePath));
		} catch (IOException ex) {}
	}
	
	public static void PrintPosTable(String documentTitle, JTable jTable){
		int[][] columnsIndex = new int[1][jTable.getColumnCount()];
		for(int i = 0; i < jTable.getColumnCount(); ++i){
			columnsIndex[0][i] = i;
		}
		PrintPosTable(documentTitle, jTable, columnsIndex, null);
	}
	
	public static void PrintPosTable(String documentTitle, JTable jTable, int[] columnIndex){
		int[][] columnsIndex = new int[1][columnIndex.length];
		for(int i = 0; i < columnIndex.length; ++i){
			columnsIndex[0][i] = columnIndex[i];
		}
		PrintPosTable(documentTitle, jTable, columnsIndex, null);
	}
	
	public static void PrintPosTable(String documentTitle, JTable jTable, int[] columnIndex, PrintTableExtraData extraData){
		int[][] columnsIndex = new int[1][columnIndex.length];
		for(int i = 0; i < columnIndex.length; ++i){
			columnsIndex[0][i] = columnIndex[i];
		}
		PrintPosTable(documentTitle, jTable, columnsIndex, extraData);
	}
	
	public static void PrintPosTable(String documentTitle, JTable jTable, int[][] columnsIndex, PrintTableExtraData extraData){
		ClientAppSettings.LoadSettings();
		if(!ClientAppSettings.GetBoolean(Values.AppSettingsEnum.SETTINGS_PRINTER_ON_INVOICES.ordinal())){
			ClientAppLogger.GetInstance().ShowMessage("POS printer za ispis računa nije uključen. Provjerite postavke printera.");
			return;
		}
		String printerName = ClientAppSettings.GetString(Values.AppSettingsEnum.SETTINGS_PRINTER_LOCAL_INVOICES.ordinal());
		String timestamp = new SimpleDateFormat("dd.MM.yyyy.").format(new Date()) + " " + new SimpleDateFormat("HH:mm:ss").format(new Date());
		TableModel tableModel = jTable.getModel();
		
		// Init
		PosPrintJob posPrintJob = GetPosPrintJob();
		posPrintJob.Init();
		
		// Logo
		if (ClientAppSettings.GetInt(Values.AppSettingsEnum.SETTINGS_EMPTY_LINES_COUNT.ordinal()) != 0){
			posPrintJob.Feed(ClientAppSettings.GetInt(Values.AppSettingsEnum.SETTINGS_EMPTY_LINES_COUNT.ordinal()));
		}
		
		// Header
		posPrintJob.AlignCenter();
		for(int i = 0; i < headerValuesPos.length; ++i){
			if(!"".equals(ClientAppSettings.GetString(headerValuesPos[i].ordinal()).trim())){
				if(i == 1){
					posPrintJob.Bold(true);
				}
				
				posPrintJob.AddText(ClientAppSettings.GetString(headerValuesPos[i].ordinal()).trim(), true);
				
				if(i == 1){
					posPrintJob.Bold(false);
				}
			}
		}
		posPrintJob.Feed(2);
		
		// Title
		posPrintJob.AlignRight();
		posPrintJob.AddText(timestamp, true);
		posPrintJob.NewLine();
		
		posPrintJob.AlignLeft();
		posPrintJob.AddText(documentTitle, true);
		
		// Extra data
		if(extraData != null){
			posPrintJob.NewLine();
			for (int i = 0; i < extraData.headerList.size(); ++i){
				posPrintJob.AddText(extraData.headerList.get(i).getKey() + extraData.headerList.get(i).getValue(), true);
			}
			posPrintJob.NewLine();
		}
		
		// Line separator
		posPrintJob.LineSeparator();
		
		// Calculate column widths
		int[][] columnsWidth = new int[columnsIndex.length][];
		for (int i = 0; i < columnsIndex.length; ++i){
			columnsWidth[i] = new int[columnsIndex[i].length];
			
			int totalRowWidth = 0;
			int columnsWidthSum = 0;
			for (int j = 0; j < columnsIndex[i].length; ++j){
				totalRowWidth += jTable.getColumnModel().getColumn(columnsIndex[i][j]).getPreferredWidth();
			}
			for (int j = 0; j < columnsIndex[i].length; ++j){
				columnsWidth[i][j] = jTable.getColumnModel().getColumn(columnsIndex[i][j]).getPreferredWidth() * posPrintJob.GetLineCharCount() / totalRowWidth;
				columnsWidthSum += columnsWidth[i][j];
			}
			// Add int round errors to last column
			columnsWidth[i][columnsWidth[i].length - 1] += (posPrintJob.GetLineCharCount() - columnsWidthSum);
		}
		
		// Table header
		for (int i = 0; i < columnsIndex.length; ++i){
			String tableHeadersString = "";
			for (int j = 0; j < columnsIndex[i].length; ++j){
				String tableHeader = tableModel.getColumnName(columnsIndex[i][j]);
				
				// Multi row first column offset
				if(j == 0){
					tableHeader = GetMultiCharacterString(2 * i, " ") + tableHeader;
				}
				
				// Align last column right - if one row header
				if(columnsIndex.length == 1 && j == columnsIndex[i].length - 1 && j != 0){
					if(tableHeader.length() <= columnsWidth[i][j]){
						tableHeadersString += GetMultiCharacterString(columnsWidth[i][j] - tableHeader.length(), " ") + tableHeader;
					} else {
						tableHeadersString += " " + tableHeader.substring(0, columnsWidth[i][j] - 1);
					}
				} else {
					if(tableHeader.length() <= columnsWidth[i][j]){
						tableHeadersString += tableHeader + GetMultiCharacterString(columnsWidth[i][j] - tableHeader.length(), " ");
					} else {
						tableHeadersString += tableHeader.substring(0, columnsWidth[i][j] - 1) + " ";
					}
				}
			}
			posPrintJob.AddText(tableHeadersString, true);
		}
		
		// Line separator
		posPrintJob.DoubleLineSeparator();
		
		// Table
		for (int i = 0; i < jTable.getRowCount(); ++i){
			for (int j = 0; j < columnsIndex.length; ++j){
				String tableRowString = "";
				for (int k = 0; k < columnsIndex[j].length; ++k){
					String cellValue = String.valueOf(tableModel.getValueAt(jTable.convertRowIndexToModel(i), columnsIndex[j][k]));
					if("null".equals(cellValue)){
						cellValue = "";
					}
					
					// Multi row first column offset
					if(k == 0){
						cellValue = GetMultiCharacterString(2 * j, " ") + cellValue;
					}
					
					// Align last column right - if one row header
					if(columnsIndex.length == 1 && k == columnsIndex[j].length - 1 && k != 0){
						if(cellValue.length() <= columnsWidth[j][k]){
							tableRowString += GetMultiCharacterString(columnsWidth[j][k] - cellValue.length(), " ") + cellValue;
						} else {
							tableRowString += " " + cellValue.substring(0, columnsWidth[j][k] - 1);
						}
					} else {
						if(cellValue.length() <= columnsWidth[j][k]){
							tableRowString += cellValue + GetMultiCharacterString(columnsWidth[j][k] - cellValue.length(), " ");
						} else {
							tableRowString += cellValue.substring(0, columnsWidth[j][k] - 1) + " ";
						}
					}
				}
				posPrintJob.AddText(tableRowString, true);
			}
		}
		
		// Extra data
		if(extraData != null){
			posPrintJob.LineSeparator();
			for (int i = 0; i < extraData.footerList.size(); ++i){
				posPrintJob.AddText(extraData.footerList.get(i).getKey() + extraData.footerList.get(i).getValue(), true);
			}
		}
		
		// Footer
		posPrintJob.AlignCenter();
		posPrintJob.Feed(2);
		for(int i = 0; i < footerValuesPos.length; ++i){
			if(!"".equals(ClientAppSettings.GetString(footerValuesPos[i].ordinal()).trim())){
				posPrintJob.AddText(ClientAppSettings.GetString(footerValuesPos[i].ordinal()).trim(), true);
			}
		}
		posPrintJob.Feed(3);
		posPrintJob.Cut();
		
		String posPrintJobString = posPrintJob.GetCommandSet();
		
		try {       
			AttributeSet attrSet = new HashPrintServiceAttributeSet(new PrinterName(printerName, null));
			DocPrintJob job = PrintServiceLookup.lookupPrintServices(null, attrSet)[0].createPrintJob();       
			DocFlavor flavor = DocFlavor.BYTE_ARRAY.AUTOSENSE;
			Doc doc = new SimpleDoc(posPrintJobString.getBytes(StandardCharsets.ISO_8859_1), flavor, null);
			job.print(doc, null);
		} catch (Exception e) {
			ClientAppLogger.GetInstance().ShowErrorLog(e);
		}
	}
	
	public static void PrintPosTableAllNormatives(String tableName, JTable jTable){
		ClientAppSettings.LoadSettings();
		if(!ClientAppSettings.GetBoolean(Values.AppSettingsEnum.SETTINGS_PRINTER_ON_INVOICES.ordinal())){
			ClientAppLogger.GetInstance().ShowMessage("POS printer za ispis računa nije uključen. Provjerite postavke printera.");
			return;
		}
		String printerName = ClientAppSettings.GetString(Values.AppSettingsEnum.SETTINGS_PRINTER_LOCAL_INVOICES.ordinal());
		String timestamp = new SimpleDateFormat("dd.MM.yyyy.").format(new Date()) + " " + new SimpleDateFormat("HH:mm:ss").format(new Date());
		TableModel tableModel = jTable.getModel();
		int columnsCount = 5;
		
		// Init
		PosPrintJob posPrintJob = GetPosPrintJob();
		posPrintJob.Init();
		
		// Logo
		if (ClientAppSettings.GetInt(Values.AppSettingsEnum.SETTINGS_EMPTY_LINES_COUNT.ordinal()) != 0){
			posPrintJob.Feed(ClientAppSettings.GetInt(Values.AppSettingsEnum.SETTINGS_EMPTY_LINES_COUNT.ordinal()));
		}
		
		// Header
		posPrintJob.AlignCenter();
		for(int i = 0; i < headerValuesPos.length; ++i){
			if(!"".equals(ClientAppSettings.GetString(headerValuesPos[i].ordinal()).trim())){
				if(i == 1){
					posPrintJob.Bold(true);
				}
				
				posPrintJob.AddText(ClientAppSettings.GetString(headerValuesPos[i].ordinal()).trim(), true);
				
				if(i == 1){
					posPrintJob.Bold(false);
				}
			}
		}
		posPrintJob.Feed(2);
		
		// Title
		posPrintJob.AlignRight();
		posPrintJob.AddText(timestamp, true);
		posPrintJob.NewLine();
		
		posPrintJob.AlignLeft();
		posPrintJob.AddText(tableName, true);
		posPrintJob.LineSeparator();
		
		// Calculate column widths
		int[] columnWidth = new int[columnsCount];
		columnWidth[0] = posPrintJob.GetLineCharCount() * 20 / 100;
		columnWidth[1] = posPrintJob.GetLineCharCount()  - columnWidth[0];
		
		columnWidth[2] = columnWidth[0] + posPrintJob.GetLineCharCount() * 40 / 100;
		columnWidth[3] = posPrintJob.GetLineCharCount() * 20 / 100;
		columnWidth[4] = posPrintJob.GetLineCharCount() - columnWidth[2] - columnWidth[3];
		
		// Table header
		String tableHeadersString1 = "";
		String tableHeadersString2 = "";
		for (int i = 0; i < columnsCount; ++i){
			String tableHeader = tableModel.getColumnName(i);
			if(i == 2) {
				tableHeader = GetMultiCharacterString(columnWidth[0], " ") + tableHeader;
			}
			if(i < 2){
				if(tableHeader.length() <= columnWidth[i]){
					tableHeadersString1 += tableHeader + GetMultiCharacterString(columnWidth[i] - tableHeader.length(), " ");
				} else {
					tableHeadersString1 += tableHeader.substring(0, columnWidth[i] - 1) + " ";
				}
			} else {
				if(tableHeader.length() <= columnWidth[i]){
					tableHeadersString2 += tableHeader + GetMultiCharacterString(columnWidth[i] - tableHeader.length(), " ");
				} else {
					tableHeadersString2 += tableHeader.substring(0, columnWidth[i] - 1) + " ";
				}
			}
		}
		posPrintJob.AddText(tableHeadersString1, true);
		posPrintJob.AddText(tableHeadersString2, true);
		posPrintJob.DoubleLineSeparator();
		
		// Table
		for (int i = 0; i < jTable.getRowCount(); ++i){
			String tableRowString = "";
			boolean isArticle = true;
			if(i > 0){
				String cellValueCurrent = String.valueOf(tableModel.getValueAt(jTable.convertRowIndexToModel(i), 0));
				String cellValuePrevious = String.valueOf(tableModel.getValueAt(jTable.convertRowIndexToModel(i-1), 0));
				isArticle = !cellValuePrevious.equals(cellValueCurrent);
			}
			
			if(isArticle){
				if(i != 0){
					posPrintJob.LineSeparator();
				}
				String cellValue0 = String.valueOf(tableModel.getValueAt(jTable.convertRowIndexToModel(i), 0));
				String cellValue1 = String.valueOf(tableModel.getValueAt(jTable.convertRowIndexToModel(i), 1));
				if(cellValue0.length() <= columnWidth[0]){
					tableRowString += cellValue0 + GetMultiCharacterString(columnWidth[0] - cellValue0.length(), " ");
				} else {
					tableRowString += cellValue0.substring(0, columnWidth[0] - 1) + " ";
				}
				if(cellValue1.length() <= columnWidth[1]){
					tableRowString += cellValue1 + GetMultiCharacterString(columnWidth[1] - cellValue1.length(), " ");
				} else {
					tableRowString += cellValue1.substring(0, columnWidth[1] - 1) + " ";
				}
				
				posPrintJob.AddText(tableRowString, true);
				tableRowString = "";
			}
			
			String cellValue2 = GetMultiCharacterString(columnWidth[0], " ") + String.valueOf(tableModel.getValueAt(jTable.convertRowIndexToModel(i), 2));
			String cellValue3 = String.valueOf(tableModel.getValueAt(jTable.convertRowIndexToModel(i), 3));
			String cellValue4 = String.valueOf(tableModel.getValueAt(jTable.convertRowIndexToModel(i), 4));
			if(cellValue2.length() <= columnWidth[2]){
				tableRowString += cellValue2 + GetMultiCharacterString(columnWidth[2] - cellValue2.length(), " ");
			} else {
				tableRowString += cellValue2.substring(0, columnWidth[2] - 1) + " ";
			}
			if(cellValue3.length() <= columnWidth[3]){
				tableRowString += cellValue3 + GetMultiCharacterString(columnWidth[3] - cellValue3.length(), " ");
			} else {
				tableRowString += cellValue3.substring(0, columnWidth[3] - 1) + " ";
			}
			if(cellValue4.length() <= columnWidth[4]){
				tableRowString += cellValue4 + GetMultiCharacterString(columnWidth[4] - cellValue4.length(), " ");
			} else {
				tableRowString += cellValue4.substring(0, columnWidth[4] - 1) + " ";
			}

			posPrintJob.AddText(tableRowString, true);
		}
		
		// Footer
		posPrintJob.AlignCenter();
		posPrintJob.Feed(2);
		for(int i = 0; i < footerValuesPos.length; ++i){
			if(!"".equals(ClientAppSettings.GetString(footerValuesPos[i].ordinal()).trim())){
				posPrintJob.AddText(ClientAppSettings.GetString(footerValuesPos[i].ordinal()).trim(), true);
			}
		}
		posPrintJob.Feed(3);
		posPrintJob.Cut();
		
		String posPrintJobString = posPrintJob.GetCommandSet();
		
		try {       
			AttributeSet attrSet = new HashPrintServiceAttributeSet(new PrinterName(printerName, null));
			DocPrintJob job = PrintServiceLookup.lookupPrintServices(null, attrSet)[0].createPrintJob();       
			DocFlavor flavor = DocFlavor.BYTE_ARRAY.AUTOSENSE;
			Doc doc = new SimpleDoc(posPrintJobString.getBytes(StandardCharsets.ISO_8859_1), flavor, null);
			job.print(doc, null);
		} catch (Exception e) {
			ClientAppLogger.GetInstance().ShowErrorLog(e);
		}
	}
	
	public static void PrintPosInvoice(Invoice invoice, int printerType) { 
		try {
			PrintPosInvoice(invoice, printerType, -1);
		} catch (IOException ex) {
			Logger.getLogger(PrintUtils.class.getName()).log(Level.SEVERE, null, ex);
		}
	}
       
	
	public static void PrintPosInvoice(Invoice invoice, int printerType, int tableIndex) throws IOException{
		ClientAppSettings.LoadSettings();
		String printerName = "";
		boolean showPrices = true;
		boolean isKitchenBar = false;
                
		if(printerType == Values.POS_PRINTER_TYPE_INVOICE){
			if(!ClientAppSettings.GetBoolean(Values.AppSettingsEnum.SETTINGS_PRINTER_ON_INVOICES.ordinal())){
				ClientAppLogger.GetInstance().ShowMessage("POS printer za ispis računa nije uključen. Provjerite postavke printera.");
				return;
			}
			
			printerName = ClientAppSettings.GetString(Values.AppSettingsEnum.SETTINGS_PRINTER_LOCAL_INVOICES.ordinal());
		} else if(printerType == Values.POS_PRINTER_TYPE_BAR){
			if(!ClientAppSettings.GetBoolean(Values.AppSettingsEnum.SETTINGS_PRINTER_ON_BAR.ordinal())){
				ClientAppLogger.GetInstance().ShowMessage("POS printer za ispis na šank nije uključen. Provjerite postavke printera.");
				return;
			}
			
			printerName = ClientAppSettings.GetString(Values.AppSettingsEnum.SETTINGS_PRINTER_LOCAL_BAR.ordinal());
			showPrices = ClientAppSettings.GetBoolean(Values.AppSettingsEnum.SETTINGS_BUTTON_SENDTOBAR_PRICE.ordinal());
			isKitchenBar = true;
		} else if(printerType == Values.POS_PRINTER_TYPE_KITCHEN){
			if(!ClientAppSettings.GetBoolean(Values.AppSettingsEnum.SETTINGS_PRINTER_ON_KITCHEN.ordinal())){
				ClientAppLogger.GetInstance().ShowMessage("POS printer za ispis u kuhinju nije uključen. Provjerite postavke printera.");
				return;
			}
			
			printerName = ClientAppSettings.GetString(Values.AppSettingsEnum.SETTINGS_PRINTER_LOCAL_KITCHEN.ordinal());
			showPrices = ClientAppSettings.GetBoolean(Values.AppSettingsEnum.SETTINGS_BUTTON_SENDTOKITCHEN_PRICE.ordinal());
			isKitchenBar = true;
		}
		
		
		// Init
		PosPrintJob posPrintJob = GetPosPrintJob(printerType);
		posPrintJob.Init();
                ClientAppLogger.GetInstance().LogMessage("POS Print job is initialized ");

		
		// Logo
		if (ClientAppSettings.GetInt(Values.AppSettingsEnum.SETTINGS_EMPTY_LINES_COUNT.ordinal()) != 0){
			posPrintJob.Feed(ClientAppSettings.GetInt(Values.AppSettingsEnum.SETTINGS_EMPTY_LINES_COUNT.ordinal()));
		}
		
		// Logo top
		String logoPath1 = "logo1.bmp";
		//String logoPath1 = "logo1.bmp";
		if (new File(logoPath1).exists()){
			posPrintJob.AlignCenter();
			posPrintJob.AddText(PrintLogoUtils.GetLogoString(logoPath1), true);
		}
		
		// Header
		posPrintJob.AlignCenter();
		for(int i = 0; i < headerValuesPos.length; ++i){
			if(!"".equals(ClientAppSettings.GetString(headerValuesPos[i].ordinal()).trim())){
				if(i == 1){
					posPrintJob.Bold(true);
				}
				
				posPrintJob.AddText(ClientAppSettings.GetString(headerValuesPos[i].ordinal()).trim(), true);
				
				if(i == 1){
					posPrintJob.Bold(false);
				}
			}
		}
		posPrintJob.Feed(2);
		
		// Title
		posPrintJob.AlignLeft();
		if(isKitchenBar){
			posPrintJob.AddText("Stol:  " + (tableIndex + 1), true);
			posPrintJob.AddText("Kasa:  " + invoice.cashRegisterNumber, true);
			posPrintJob.AddText("Datum: " + new SimpleDateFormat("dd.MM.yyyy. HH:mm:ss").format(invoice.date), true);
			posPrintJob.AddText("Oznaka djelatnika: " + invoice.staffId + "-" + invoice.staffName, true);
		} else {
			if(invoice.paymentMethodType == Values.PAYMENT_METHOD_TYPE_OFFER){
				posPrintJob.AddText("Broj ponude: " + invoice.specialNumber + "/" + invoice.officeTag + "/" + invoice.cashRegisterNumber, true);
			} else if(invoice.paymentMethodType == Values.PAYMENT_METHOD_TYPE_ISSUE_SLIP){
				posPrintJob.AddText("Broj izdatnice: " + invoice.specialNumber + "/" + invoice.officeTag, true);
			} else if(invoice.paymentMethodType == Values.PAYMENT_METHOD_TYPE_SUBTOTAL){
				//posPrintJob.AddText("Broj predračuna: " + invoice.specialNumber + "/" + invoice.officeTag + "/" + invoice.cashRegisterNumber, true);
			} else {
				posPrintJob.AddText("Broj računa: " + invoice.invoiceNumber + "/" + invoice.officeTag + "/" + invoice.cashRegisterNumber, true);
			}
			posPrintJob.AddText("Datum:        " + new SimpleDateFormat("dd.MM.yyyy. HH:mm:ss").format(invoice.date), true);
			posPrintJob.AddText("Oznaka djelatnika: " + invoice.staffId + "-" + invoice.staffName, true);
		}
		int chDate = CheckDate(invoice.date);
                
                ClientAppLogger.GetInstance().LogMessage("invoice client name is: " + invoice.clientName);
                ClientAppLogger.GetInstance().LogMessage("invoice note is: " + invoice.note);
                ClientAppLogger.GetInstance().LogMessage("invoice office tag is: " + invoice.officeTag);
                ClientAppLogger.GetInstance().LogMessage("invoice payment method is: " + invoice.paymentMethodType2);
                
		// Client data
		if(invoice.clientId != -1){
			posPrintJob.NewLine();
			posPrintJob.AddText("Kupac: " + invoice.clientName, true);
			posPrintJob.AddText("OIB kupca: " + invoice.clientOIB, true);
                        posPrintJob.AddText("Adresa: " + invoice.note, true);
		}
                
                //invoice.note = "";
                
		// Invoice copy
		if(printerType == Values.POS_PRINTER_TYPE_INVOICE){
			if(invoice.isCopy && invoice.paymentMethodType != Values.PAYMENT_METHOD_TYPE_OFFER && invoice.paymentMethodType != Values.PAYMENT_METHOD_TYPE_SUBTOTAL && invoice.paymentMethodType != Values.PAYMENT_METHOD_TYPE_ISSUE_SLIP){
				posPrintJob.NewLine();
				posPrintJob.Bold(true);
				posPrintJob.AddText("KOPIJA RAČUNA", true);
				posPrintJob.Bold(false);
			}
			if(invoice.paymentMethodType == Values.PAYMENT_METHOD_TYPE_SUBTOTAL){
				posPrintJob.NewLine();
				posPrintJob.Bold(true);
				posPrintJob.AddText("STANJE STOLA", true);
				posPrintJob.Bold(false);
			}
		} else if(printerType == Values.POS_PRINTER_TYPE_BAR){
			posPrintJob.NewLine();
			posPrintJob.Bold(true);
			posPrintJob.AlignCenter();
			posPrintJob.AddText("NARUDŽBA ZA ŠANK", true);
			posPrintJob.AlignLeft();
			posPrintJob.Bold(false);
		} else if(printerType == Values.POS_PRINTER_TYPE_KITCHEN){
			posPrintJob.NewLine();
			posPrintJob.Bold(true);
			posPrintJob.AlignCenter();
			posPrintJob.AddText("NARUDŽBA ZA KUHINJU", true);
			posPrintJob.AlignLeft();
			posPrintJob.Bold(false);
		}
		posPrintJob.LineSeparator();
		
		// Calculate column widths
		int[][] columnsWidth = new int[][]{
			new int[]{
				posPrintJob.GetLineCharCount()
			},
			new int[]{
				posPrintJob.GetLineCharCount() * 40 / 100, 
				posPrintJob.GetLineCharCount() * 30 / 100, 
				posPrintJob.GetLineCharCount() - posPrintJob.GetLineCharCount() * 40 / 100 - posPrintJob.GetLineCharCount() * 30 / 100
			}
		};
		
		// Table header
		posPrintJob.AddText("Stavka", true);
		String invoiceHeaderLine2 = GetMultiCharacterString(columnsWidth[1][0] - "Količina".length(), " ") + "Količina";
		invoiceHeaderLine2 += GetMultiCharacterString(columnsWidth[1][1] - "Cijena".length(), " ") + "Cijena";
		invoiceHeaderLine2 += GetMultiCharacterString(columnsWidth[1][2] - "Iznos".length(), " ") + "Iznos";
		posPrintJob.AddText(invoiceHeaderLine2, true);
		
		// Line separator
		posPrintJob.DoubleLineSeparator();
		
		// Table
		for (int i = 0; i < invoice.items.size(); ++i){
			if(!"".equals(invoice.items.get(i).itemNote)){
				String noteString = " - " + invoice.items.get(i).itemNote;
				if(noteString.length() <= posPrintJob.GetLineCharCount()){
					posPrintJob.AddText(noteString, true);
				} else {
					posPrintJob.AddText(noteString.substring(0, posPrintJob.GetLineCharCount()), true);
				}
				continue;
			}
			
			if(invoice.items.get(i).itemName.length() <= columnsWidth[0][0]){
				posPrintJob.AddText(invoice.items.get(i).itemName, true);
			} else {
				posPrintJob.AddText(invoice.items.get(i).itemName.substring(0, columnsWidth[0][0]), true);
			}
                        
			
			String invoiceItemLine2 = "";                        
			String itemAmountString = Float.toString(invoice.items.get(i).itemAmount);
                        String invoiceItemLine2Eur = "";
                        String itemAmountStringEur = null;
                        if(chDate == 10 || chDate == 30){
                            itemAmountStringEur = PrintUtils.CalculateExchangeRate(invoice.items.get(i).itemAmount) + " Eur";
                        }else if(chDate == 20){
                            itemAmountStringEur = PrintUtils.EurToKn(invoice.items.get(i).itemAmount) + " Kn";
                        }

			if(itemAmountString.length() <= columnsWidth[1][0]){
				invoiceItemLine2 += GetMultiCharacterString(columnsWidth[1][0] - itemAmountString.length(), " ") + itemAmountString;
			} else {
				itemAmountString = itemAmountString.substring(0, columnsWidth[1][0] - 1) + " ";
				invoiceItemLine2 += itemAmountString;
			}
                        // ako chDate = 30 onda je datum veći od 01.01.2024 pa ne treba dvojni prikazivanje na racunu
                        if(chDate != 30){
                            if(itemAmountStringEur.length() <= columnsWidth[1][0]){
                                invoiceItemLine2Eur += GetMultiCharacterString(columnsWidth[1][0], " ");
                            }else{
                                //itemAmountStringEur = itemAmountStringEur.substring(0, columnsWidth[1][0]-1) + " ";
                                //invoiceItemLine2Eur += itemAmountStringEur;
                            }
                        }
			
			if(showPrices && invoice.paymentMethodType != Values.PAYMENT_METHOD_TYPE_SUBTOTAL){
				String itemPriceString = ClientAppUtils.FloatToPriceString(invoice.items.get(i).itemPrice);
                                String itemPriceStringEur = null;
                                if(chDate == 10){
                                    itemPriceStringEur = PrintUtils.CalculateExchangeRate(invoice.items.get(i).itemPrice);
                                }else if(chDate == 20){
                                    itemPriceStringEur = PrintUtils.EurToKn(invoice.items.get(i).itemPrice);
                                }
				if(itemPriceString.length() <= columnsWidth[1][1]){
					invoiceItemLine2 += GetMultiCharacterString(columnsWidth[1][1] - itemPriceString.length(), " ") + itemPriceString;
				} else {
					itemPriceString = itemPriceString.substring(0, columnsWidth[1][1] - 1) + " ";
					invoiceItemLine2 += itemPriceString;
				}
                                if(chDate != 30){
                                    if(itemPriceStringEur.length() <= columnsWidth[1][1]){
                                        invoiceItemLine2Eur += GetMultiCharacterString(columnsWidth[1][1] - itemPriceStringEur.length(), " ") + itemPriceStringEur;
                                    }else{
                                        itemPriceStringEur = itemPriceStringEur.substring(0, columnsWidth[1][1] - 1) + " ";
                                            invoiceItemLine2Eur += itemPriceStringEur;
                                    }
                                }

				String itemTotalString = ClientAppUtils.FloatToPriceString(invoice.items.get(i).itemPrice * invoice.items.get(i).itemAmount);
                                String itemTotalStringEur = null;
                                if(chDate == 10){
                                    itemTotalStringEur = PrintUtils.CalculateExchangeRate(invoice.items.get(i).itemPrice * invoice.items.get(i).itemAmount);
                                }else if(chDate == 20){
                                    itemTotalStringEur = PrintUtils.EurToKn(invoice.items.get(i).itemPrice * invoice.items.get(i).itemAmount);
                                }
				if(itemTotalString.length() <= columnsWidth[1][2]){
					invoiceItemLine2 += GetMultiCharacterString(columnsWidth[1][2] - itemTotalString.length(), " ") + itemTotalString;
				} else {
					itemTotalString = itemTotalString.substring(0, columnsWidth[1][2]);
					invoiceItemLine2 += itemTotalString;
				}
                                if(chDate != 30){
                                    if(itemTotalStringEur.length() <= columnsWidth[1][2]){
                                            invoiceItemLine2Eur += GetMultiCharacterString(columnsWidth[1][2] - itemTotalStringEur.length(), " ") + itemTotalStringEur;
                                    } else {
                                            itemTotalStringEur = itemTotalStringEur.substring(0, columnsWidth[1][2]);
                                            invoiceItemLine2Eur += itemTotalStringEur;
                                    }
                                }
			}
			
			posPrintJob.AddText(invoiceItemLine2, true);
                        //posPrintJob.NewLine();
                        //posPrintJob.AddText(invoiceItemLine2Eur, true);
			
			// Item discount
			String discountString = "";
                        String discountStringEur = "";
			if(invoice.items.get(i).discountPercentage != 0f){
				String discountPrefix = "Popust " + invoice.items.get(i).discountPercentage + " %";
				String discountSufix = ClientAppUtils.FloatToPriceString(-1f * invoice.items.get(i).itemPrice * invoice.items.get(i).itemAmount * invoice.items.get(i).discountPercentage / 100f);
				discountString = discountPrefix + GetMultiCharacterString(posPrintJob.GetLineCharCount() - discountPrefix.length() - discountSufix.length(), " ") + discountSufix;
			} else if(invoice.items.get(i).discountValue != 0f){
                                String disStringEur = null;
                                if(chDate == 10){
                                    disStringEur = PrintUtils.CalculateExchangeRate(invoice.items.get(i).discountValue) + " Eur/kom";
                                }else if(chDate == 20){
                                    disStringEur = PrintUtils.EurToKn(invoice.items.get(i).discountValue) + " Kn/kom";
                                }
				String discountPrefix = null;
                                if(chDate == 10){
                                    discountPrefix = "Popust " + invoice.items.get(i).discountValue + " kn/kom";
                                }else if(chDate == 20 || chDate == 30){
                                    discountPrefix = "Popust " + invoice.items.get(i).discountValue + " eur/kom";
                                }
				String discountSufix = ClientAppUtils.FloatToPriceString(-1f * invoice.items.get(i).itemAmount * invoice.items.get(i).discountValue);
				discountString = discountPrefix + GetMultiCharacterString(posPrintJob.GetLineCharCount() - discountPrefix.length() - discountSufix.length(), " ") + discountSufix;
                                if(chDate != 30){
                                    discountStringEur = GetMultiCharacterString(posPrintJob.GetLineCharCount() - disStringEur.length(), " ") + disStringEur;
                                }
                                
                                
			}
			if(!"".equals(discountString) && showPrices && invoice.paymentMethodType != Values.PAYMENT_METHOD_TYPE_SUBTOTAL){
				posPrintJob.AddText(discountString, true);
//                                if(!"".equals(discountStringEur)){
//                                    posPrintJob.NewLine();
//                                    posPrintJob.AddText(discountStringEur, true);
//                                }
			}
		}
		
		posPrintJob.LineSeparator();
		
		float totalPriceWithDiscount = 0f;
		if(showPrices){
			// Invoice discount
			String discountString = "";
                        String discountStringEur = "";
			if(invoice.discountPercentage != 0f){
				String discountPrefix = "Popust " + invoice.discountPercentage + " %";
				String discountSufix = ClientAppUtils.FloatToPriceString(-1f * invoice.totalPrice * invoice.discountPercentage / 100f);
				discountString = discountPrefix + GetMultiCharacterString(posPrintJob.GetLineCharCount() - discountPrefix.length() - discountSufix.length(), " ") + discountSufix;
			} else if(invoice.discountValue != 0f){
                            String disStringEur = null;
                            String discountPrefix = null;
                            if(chDate == 10){
                                disStringEur = PrintUtils.CalculateExchangeRate(invoice.discountValue) + " Eur";
                                discountPrefix = "Popust " + invoice.discountValue + " kn";
                            }else if(chDate == 20){
                                disStringEur = PrintUtils.EurToKn(invoice.discountValue) + " kn";
                                discountPrefix = "Popust " + invoice.discountValue + " eur";
                            }
				String discountSufix = ClientAppUtils.FloatToPriceString(-1f * invoice.discountValue);                                
				discountString = discountPrefix + GetMultiCharacterString(posPrintJob.GetLineCharCount() - discountPrefix.length() - discountSufix.length(), " ") + discountSufix;
                                if(chDate != 30){
                                    discountStringEur = GetMultiCharacterString(posPrintJob.GetLineCharCount() - disStringEur.length(), " ") + disStringEur;
                                }
                                
			}
			if(!"".equals(discountString) && invoice.paymentMethodType != Values.PAYMENT_METHOD_TYPE_SUBTOTAL){
				posPrintJob.AddText(discountString, true);
//                                if(!"".equals(discountStringEur))
//                                {
//                                    posPrintJob.NewLine();
//                                    posPrintJob.AddText(discountStringEur, true);
//                                }                              
                                
				//posPrintJob.LineSeparator();
			}

			// Total
			totalPriceWithDiscount = invoice.totalPrice * (100f - invoice.discountPercentage) / 100f - invoice.discountValue;
			String totalPrefix = "Ukupno: ";
                        String totalPriceWithDiscountEur = null;
                        String totalSufix = null;
                        if(chDate == 10 || chDate == 30){
                            totalPriceWithDiscountEur = PrintUtils.CalculateExchangeRate(invoice.totalPrice) + " Eur";
                            totalSufix = ClientAppUtils.FloatToPriceString(totalPriceWithDiscount) + " eur";
                        }else if(chDate == 20){
                            totalPriceWithDiscountEur = PrintUtils.EurToKn(totalPriceWithDiscount) + " kn";
                            totalSufix = ClientAppUtils.FloatToPriceString(totalPriceWithDiscount) + " eur";
                        }

			if(invoice.paymentMethodType != Values.PAYMENT_METHOD_TYPE_SUBTOTAL){
                                String totalStringEur = "";
				//String totalStringEur = totalPrefix + GetMultiCharacterString(posPrintJob.GetLineCharCount() / 2 - totalPrefix.length() - totalSufix.length(), " ") + totalSufix;
                                if(chDate == 30){
                                    totalStringEur = totalPrefix + GetMultiCharacterString(posPrintJob.GetLineCharCount() / 2 - totalPrefix.length() - totalSufix.length(), " ") + totalSufix;
                                }
				posPrintJob.DoubleSize(true);
				posPrintJob.AddText(totalStringEur, true);
                                posPrintJob.DoubleSize(false);
                                posPrintJob.NewLine();
                                if(chDate != 30){
                                    //posPrintJob.AddText(totalStringEur, true);
                                }
				
				posPrintJob.NewLine();
			}
                        if(chDate != 30){       
                            //posPrintJob.AddText("Tečaj Kn/EUR: " + PrintUtils.strExchangeRate , true);
                        }
		}
		
		if(!isKitchenBar && invoice.paymentMethodType != Values.PAYMENT_METHOD_TYPE_SUBTOTAL){
			// Payment method
			if(invoice.paymentMethodType2 == -1){
				String paymentMethodPrefix = "Način plaćanja: ";
				String paymentMethodSufix = invoice.paymentMethodName;
				String paymentMethodString = paymentMethodPrefix + GetMultiCharacterString(posPrintJob.GetLineCharCount() - paymentMethodPrefix.length() - paymentMethodSufix.length(), " ") + paymentMethodSufix;
				posPrintJob.Bold(true);
				posPrintJob.AddText(paymentMethodString, true);
				posPrintJob.Bold(false);
			} else {
				float totalPriceWithDiscount2 = invoice.paymentAmount2 * (100f - invoice.discountPercentage) / 100f - invoice.discountValue;
				float totalPriceWithDiscount1 = totalPriceWithDiscount - totalPriceWithDiscount2;

				posPrintJob.Bold(true);
				posPrintJob.AddText("Načini plaćanja: ", true);
				posPrintJob.Bold(false);
				
				String paymentMethodPrefix1 = invoice.paymentMethodName;
				String paymentMethodSufix1 = ClientAppUtils.FloatToPriceString(totalPriceWithDiscount1);
				String paymentMethodString1 = paymentMethodPrefix1 + GetMultiCharacterString(posPrintJob.GetLineCharCount() - paymentMethodPrefix1.length() - paymentMethodSufix1.length(), " ") + paymentMethodSufix1;
				
				String paymentMethodPrefix2 = invoice.paymentMethodName2;
				String paymentMethodSufix2 = ClientAppUtils.FloatToPriceString(totalPriceWithDiscount2);
				String paymentMethodString2 = paymentMethodPrefix2 + GetMultiCharacterString(posPrintJob.GetLineCharCount() - paymentMethodPrefix2.length() - paymentMethodSufix2.length(), " ") + paymentMethodSufix2;
				
				posPrintJob.AddText(paymentMethodString1, true);
				posPrintJob.AddText(paymentMethodString2, true);
			}
			
			if(invoice.paymentMethodType == Values.PAYMENT_METHOD_TYPE_TRANSACTION_BILL){
				Calendar calendar = Calendar.getInstance();
				calendar.setTime(invoice.date);
				calendar.add(Calendar.DATE, invoice.paymentDelay);
				
				String paymentDelayPrefix = "Rok plaćanja: ";
				String paymentDelaySufix = new SimpleDateFormat("dd.MM.yyyy.").format(calendar.getTime());
				String paymentDelayString = paymentDelayPrefix + GetMultiCharacterString(posPrintJob.GetLineCharCount() - paymentDelayPrefix.length() - paymentDelaySufix.length(), " ") + paymentDelaySufix;
				posPrintJob.AddText(paymentDelayString, true);
			}
		}
		
		// Note
		if(!"".equals(invoice.note)){
			posPrintJob.NewLine();
			if(invoice.paymentMethodType == Values.PAYMENT_METHOD_TYPE_SUBTOTAL && !isKitchenBar){
				posPrintJob.DoubleSize(true);
				posPrintJob.AddText(invoice.note, true);
				posPrintJob.DoubleSize(false);
				posPrintJob.NewLine();
			} else {
				posPrintJob.AddText("Napomena: " + invoice.note, true);
			}
		}
		
		if(!isKitchenBar && invoice.paymentMethodType != Values.PAYMENT_METHOD_TYPE_SUBTOTAL){
			posPrintJob.DoubleLineSeparator();
			
			// Taxes header
			String[] taxesHeader = new String[]{"PDV", "Osnovica", "Iznos"};
			int[] taxesHeaderWidths = new int[]{
				posPrintJob.GetLineCharCount() * 35 / 100,
				posPrintJob.GetLineCharCount() * 35 / 100,
				posPrintJob.GetLineCharCount() - posPrintJob.GetLineCharCount() * 35 / 100 - posPrintJob.GetLineCharCount() * 35 / 100
			};
			String taxesHeaderLine = "";
			for (int i = 0; i < taxesHeader.length; ++i){
				String taxesHeaderString = taxesHeader[i];
				if(taxesHeaderString.length() <= taxesHeaderWidths[i]){
					if(i == 0){
						taxesHeaderLine += taxesHeaderString + GetMultiCharacterString(taxesHeaderWidths[i] - taxesHeaderString.length(), " ");
					} else{
						taxesHeaderLine += GetMultiCharacterString(taxesHeaderWidths[i] - taxesHeaderString.length(), " ") + taxesHeaderString;
					}
				} else {
					if(i == 0){
						taxesHeaderString = taxesHeaderString.substring(0, taxesHeaderWidths[i] - 1) + " ";
					} else {
						taxesHeaderString = " " + taxesHeaderString.substring(0, taxesHeaderWidths[i] - 1);
					}
					taxesHeaderLine += taxesHeaderString;
				}
			}
			posPrintJob.AddText(taxesHeaderLine, true);
			posPrintJob.LineSeparator();

			// Taxes calculation
			InvoiceTaxes invoiceTaxes = ClientAppUtils.CalculateTaxes(invoice);
			for (int taxIndex = 0; taxIndex < invoiceTaxes.taxRates.size(); ++taxIndex){
				if(invoiceTaxes.taxRates.get(taxIndex) == 0f)
					continue;
				
				String[] taxesLineStrings = new String[]{
					invoiceTaxes.taxRates.get(taxIndex) + "%", 
					ClientAppUtils.DoubleToPriceString(invoiceTaxes.taxBases.get(taxIndex)), 
					ClientAppUtils.DoubleToPriceString(invoiceTaxes.taxAmounts.get(taxIndex)), 
				};
				String taxesLine = "";
				for (int i = 0; i < taxesLineStrings.length; ++i){
					String taxesString = taxesLineStrings[i];
					if(taxesString.length() <= taxesHeaderWidths[i]){
						if(i == 0){
							taxesLine += taxesString + GetMultiCharacterString(taxesHeaderWidths[i] - taxesString.length(), " ");
						} else{
							taxesLine += GetMultiCharacterString(taxesHeaderWidths[i] - taxesString.length(), " ") + taxesString;
						}
					} else {
						if(i == 0){
							taxesString = taxesString.substring(0, taxesHeaderWidths[i] - 1) + " ";
						} else {
							taxesString = " " + taxesString.substring(0, taxesHeaderWidths[i] - 1);
						}
						taxesLine += taxesString;
					}
				}
				posPrintJob.AddText(taxesLine, true);
			}

			posPrintJob.LineSeparator();
			
			// Packaging refunds calculation
			int[] refundsHeaderWidths = new int[]{
				posPrintJob.GetLineCharCount() * 40 / 100,
				posPrintJob.GetLineCharCount() * 20 / 100,
				posPrintJob.GetLineCharCount() * 20 / 100,
				posPrintJob.GetLineCharCount() - posPrintJob.GetLineCharCount() * 20 / 100 - posPrintJob.GetLineCharCount() * 20 / 100 - posPrintJob.GetLineCharCount() * 40 / 100
			};
			PackagingRefunds packagingRefunds = ClientAppUtils.CalculatePackagingRefunds(invoice);
			boolean refundsTitlePrinted = false;
			for (int refundIndex = 0; refundIndex < packagingRefunds.refundValues.size(); ++refundIndex){
				if(packagingRefunds.refundAmounts.get(refundIndex) == 0f)
					continue;
				
				if(!refundsTitlePrinted){
					refundsTitlePrinted = true;
					posPrintJob.AddText("Povratne naknade:", true);
				}
				
				String[] refundsLineStrings = new String[]{
					"",
					packagingRefunds.refundAmounts.get(refundIndex) + " kom", 
					ClientAppUtils.FloatToPriceString(packagingRefunds.refundValues.get(refundIndex)), 
					ClientAppUtils.FloatToPriceString(packagingRefunds.refundValues.get(refundIndex) * packagingRefunds.refundAmounts.get(refundIndex)), 
				};
				String refundLine = "";
				for (int i = 0; i < refundsLineStrings.length; ++i){
					String refundsString = refundsLineStrings[i];
					if(refundsString.length() <= refundsHeaderWidths[i]){
						if(i == 0){
							refundLine += refundsString + GetMultiCharacterString(refundsHeaderWidths[i] - refundsString.length(), " ");
						} else{
							refundLine += GetMultiCharacterString(refundsHeaderWidths[i] - refundsString.length(), " ") + refundsString;
						}
					} else {
						if(i == 0){
							refundsString = refundsString.substring(0, refundsHeaderWidths[i] - 1) + " ";
						} else {
							refundsString = " " + refundsString.substring(0, refundsHeaderWidths[i] - 1);
						}
						refundLine += refundsString;
					}
				}
				posPrintJob.AddText(refundLine, true);
			}

			posPrintJob.LineSeparator();

			// ZKI and JIR
			if(ClientAppUtils.IsFiscalizationType(invoice.paymentMethodType)){
				posPrintJob.AddText("ZKI: " + invoice.zki, true);
				if(Values.DEFAULT_JIR.equals(invoice.jir)){
					posPrintJob.AddText("JIR: " + "Nije dobiven u predviđenom vremenu", true);
				} else {
					posPrintJob.AddText("JIR: " + invoice.jir, true);
				}
                                String qrData = "https://porezna.gov.hr/rn?zki=" + invoice.zki + "&datv=" +  new SimpleDateFormat("yyyyMMdd_HHmm").format(invoice.date) + "&izn=" + String.format("%.2f", invoice.totalPrice);
                                
                                posPrintJob.NewLine();
                                posPrintJob.AlignCenter();
                                posPrintJob.PrintQRCode(qrData);
                                //posPrintJob.AddText(qrData, true);
                                
                                
//                                BarcodeQRCode barcodeQRCode = new BarcodeQRCode(qrData, 200, 200, null);
//                                java.awt.Image awtImage = barcodeQRCode.createAwtImage(Color.BLACK, Color.WHITE);
//                                BufferedImage bImage = new BufferedImage(awtImage.getWidth(null), awtImage.getHeight(null), BufferedImage.TYPE_INT_RGB);
//                                Graphics2D gImage = bImage.createGraphics();
//                                gImage.drawImage(awtImage, 0, 0, null);
//                                gImage.dispose();
//                                ImageIO.write(bImage, "bmp", new File("Qr.bmp"));
                                
                                
                                
                                
//                                String qrCodePath = "Qr.bmp";
//                                //String logoPath1 = "logo1.bmp";
//                                if (new File(qrCodePath).exists()){
//                                        posPrintJob.AlignCenter();
//                                        posPrintJob.AddText(PrintLogoUtils.GetLogoString(qrCodePath), true);
//                                }
                             
                                
			} else if(invoice.paymentMethodType != Values.PAYMENT_METHOD_TYPE_OFFER && invoice.paymentMethodType != Values.PAYMENT_METHOD_TYPE_SUBTOTAL) {
				posPrintJob.AddText("Ovaj račun nije podložan fiskalizaciji", true);
			}
			
			// E invoice
			if (!"".equals(invoice.einvoiceId)){
				posPrintJob.NewLine();
				posPrintJob.AddText("Broj e-računa: " + invoice.einvoiceId, true);
			}
			
			if (!invoice.isInVatSystem){
				posPrintJob.NewLine();
				posPrintJob.AddText("Obveznik nije u sustavu PDV-a, PDV nije obračunat temeljem čl. 90 st.2 Zakona o PDV-u.", true);
			}
			
			/*if(invoice.paymentMethodType == Values.PAYMENT_METHOD_TYPE_SUBTOTAL) {
				posPrintJob.AddText("OVO NIJE FISKALIZIRANI RAČUN", true);
			}*/
			
			if(invoice.isTest){
				posPrintJob.NewLine();
				posPrintJob.Bold(true);
				posPrintJob.AddText("OVAJ RAČUN JE IZDAN U TESTNOM OKRUŽENJU", true);
				posPrintJob.Bold(false);
			}
		}
		
		if(!isKitchenBar && invoice.paymentMethodType == Values.PAYMENT_METHOD_TYPE_SUBTOTAL){
			//posPrintJob.AddText("OVO NIJE FISKALIZIRANI RAČUN", true);
			
			if(invoice.isTest){
				posPrintJob.NewLine();
				posPrintJob.Bold(true);
				posPrintJob.AddText("TESTNO OKRUŽENJE", true);
				posPrintJob.Bold(false);
			}
		}
		
		// Footer
		posPrintJob.AlignCenter();
		posPrintJob.Feed(2);
		for(int i = 0; i < footerValuesPos.length; ++i){
			if(!"".equals(ClientAppSettings.GetString(footerValuesPos[i].ordinal()).trim())){
				posPrintJob.AddText(ClientAppSettings.GetString(footerValuesPos[i].ordinal()).trim(), true);
			}
		}
			
		// Logo bottom
		String logoPath2 = "logo2.bmp";
		//String logoPath2 = "logo1.bmp";
		if (new File(logoPath2).exists()){
			posPrintJob.AlignCenter();
			posPrintJob.AddText(PrintLogoUtils.GetLogoString(logoPath2), true);
		}
		
		
		posPrintJob.Feed(3);
		posPrintJob.Cut();
                posPrintJob.OpenCaddy();
		
		String posPrintJobString = posPrintJob.GetCommandSet();
		
		try {       
			AttributeSet attrSet = new HashPrintServiceAttributeSet(new PrinterName(printerName, null));
			DocPrintJob job = PrintServiceLookup.lookupPrintServices(null, attrSet)[0].createPrintJob();       
			DocFlavor flavor = DocFlavor.BYTE_ARRAY.AUTOSENSE;
			Doc doc = new SimpleDoc(posPrintJobString.getBytes(StandardCharsets.ISO_8859_1), flavor, null);
                        
			job.print(doc, null);
		} catch (Exception e) {
			ClientAppLogger.GetInstance().ShowErrorLog(e);
		}
	}
	
	private static void PrintA4HeaderFooterPages(String tempFilePath, String filePath){
		InitFonts();
		try {
			PdfReader pdfReader = new PdfReader(tempFilePath);
			PdfStamper pdfStamper = new PdfStamper(pdfReader, new FileOutputStream(filePath));
			
			int pageCount = pdfReader.getNumberOfPages();
			for (int i = 1; i <= pageCount; i++) {
				PdfContentByte under = pdfStamper.getUnderContent(i);
				
				// Page numbers
				Phrase pageNumberPhrase = new Phrase(String.format("Stranica %d od %d", i, pageCount), cellFont); 
				String pdfTimestamp = new SimpleDateFormat("dd.MM.yyyy.").format(new Date()) + " " + new SimpleDateFormat("HH:mm:ss").format(new Date());
				Phrase timestampPhrase = new Phrase(pdfTimestamp, cellFont); 
				ColumnText.showTextAligned(under, Element.ALIGN_RIGHT, pageNumberPhrase, 559, 36, 0);
				ColumnText.showTextAligned(under, Element.ALIGN_RIGHT, timestampPhrase, 559, 842 - 36, 0);
				
				// Header
				for(int j = 0; j < headerValuesA4.length; ++j){
					Phrase phrase = new Phrase(ClientAppSettings.GetString(headerValuesA4[j].ordinal()).trim(), headerFooterFont);
					ColumnText.showTextAligned(under, Element.ALIGN_LEFT, phrase, 36, 806 - headerFooterSpace * j, 0);
				}
				
				// Footer
				for(int j = 0; j < footerValuesA4.length; ++j){
					Phrase phrase = new Phrase(ClientAppSettings.GetString(footerValuesA4[footerValuesA4.length - j - 1].ordinal()).trim(), footerFont);
					ColumnText.showTextAligned(under, Element.ALIGN_LEFT, phrase, 36, 36 + 36 + headerFooterSpace * j, 0);
				}
			}
			
			//pdfReader.close();
			//System.out.println("STAMPER: " + pdfStamper.toString());
			pdfStamper.close();
		} catch (IOException | DocumentException ex) {
			ClientAppLogger.GetInstance().ShowMessage("Došlo je do pogreške tijekom generiranja dokumenta.");
		}
	}
	
	private static void AddTwoChunkLine(String string1, String string2, Document document) throws DocumentException{
		boolean alignRight = string1.contains(ALIGN_RIGHT_SUBSTRING) || string2.contains(ALIGN_RIGHT_SUBSTRING);
		string1 = string1.replace(ALIGN_RIGHT_SUBSTRING, "");
		string2 = string2.replace(ALIGN_RIGHT_SUBSTRING, "");
		
		Paragraph paragraph = new Paragraph();
		paragraph.add(new Chunk(string1, regularFontBold));
		paragraph.add(new Chunk(string2, regularFont));
		if(alignRight){
			paragraph.setAlignment(Paragraph.ALIGN_RIGHT);
		}
		document.add(paragraph);
	}
        
        private static void AddThreeChunkLine(String zki, String zkiKey, String qr, String jirKey, String jir, Document doc)throws DocumentException, IOException
        {
            PdfPTable table = new PdfPTable(2);
            table.setWidths(new int[]{50, 220});
            table.setHorizontalAlignment(Element.ALIGN_LEFT);
            BarcodeQRCode qr_code = new BarcodeQRCode(qr, 1, 1, null);
            Image qrImg = qr_code.getImage(); 
            PdfPCell cell = new PdfPCell();
            cell.setHorizontalAlignment(Element.ALIGN_LEFT);
            cell.setBorder(0);
            cell.setImage(qrImg);
            qrImg.setDpi(700, 700);
            table.addCell(cell);
            Chunk ch1 = new Chunk(zkiKey + zki);
            Chunk ch2 = new Chunk(jirKey + jir);
            Paragraph par1 = new Paragraph();
            Paragraph par2 = new Paragraph();
            par1.add(ch1);
            par2.add(ch2);
            PdfPCell cell2 = new PdfPCell();
            cell2.setBorder(0);
            cell2.setVerticalAlignment(Element.ALIGN_MIDDLE);
            cell2.addElement(par1);
            cell2.addElement(par2);
            table.addCell(cell2);
            doc.add(table);
//            File f = new File(zki + ".bmp");
//            f.delete();
        }
	
	private static void InitFonts(){
		headerFooterFont = FontFactory.getFont(defaultFont, "Cp1250");
		headerFooterFont.setSize(12);
                
        footerFont = FontFactory.getFont(defaultFont, "Cp1250");
        footerFont.setSize(8);
		
		regularFont = FontFactory.getFont(defaultFont, "Cp1250");
		regularFont.setSize(10);
		
		regularFontBold = FontFactory.getFont(defaultFont, "Cp1250");
		regularFontBold.setStyle(Font.BOLD);
		regularFontBold.setSize(10);
		
		titleFont = FontFactory.getFont(defaultFont, "Cp1250");
		titleFont.setStyle(Font.BOLD);
		titleFont.setSize(18);
		
		cellFont = FontFactory.getFont(defaultFont, "Cp1250");
		cellFont.setSize(9);
		
		cellFontBold = FontFactory.getFont(defaultFont, "Cp1250");
		cellFontBold.setSize(9);
		cellFontBold.setStyle(Font.BOLD);
	}
	
	private static int GetHeaderLinesCountA4(){
		int headerLinesCount = 0;
		for(int i = 0; i < headerValuesA4.length; ++i){
			if(!"".equals(ClientAppSettings.GetString(headerValuesA4[i].ordinal()).trim())){
				headerLinesCount = headerValuesA4.length - i;
				break;
			}
		}
		return headerLinesCount;
	}
	
	private static int GetFooterLinesCountA4(){
		int footerLinesCount = 0;
		for(int i = 0; i < footerValuesA4.length; ++i){
			if(!"".equals(ClientAppSettings.GetString(footerValuesA4[i].ordinal()).trim())){
				footerLinesCount = footerValuesA4.length - i;
				break;
			}
		}
		return footerLinesCount;
	}
	
	private static String GetMultiCharacterString(int length, String stringChar){
		StringBuilder titleSpaces = new StringBuilder();
		for (int i = 0; i < length; i++){
			titleSpaces.append(stringChar);
		}
		return titleSpaces.toString();
	}
	
	private static PosPrintJob GetPosPrintJob(){
		return GetPosPrintJob(Values.POS_PRINTER_TYPE_INVOICE);
	}
	
	private static PosPrintJob GetPosPrintJob(int printerType){
		int settingsId = Values.AppSettingsEnum.SETTINGS_PRINTER_TYPE_INVOICES.ordinal();
		if (printerType == Values.POS_PRINTER_TYPE_BAR){
			settingsId = Values.AppSettingsEnum.SETTINGS_PRINTER_TYPE_BAR.ordinal();
		} else if (printerType == Values.POS_PRINTER_TYPE_KITCHEN){
			settingsId = Values.AppSettingsEnum.SETTINGS_PRINTER_TYPE_KITCHEN.ordinal();
		}
		
		if("Epson 42".equals(ClientAppSettings.GetString(settingsId))){
			return new PosPrintJobEpson42();
		} else if("Epson 56".equals(ClientAppSettings.GetString(settingsId))){
			return new PosPrintJobEpson56();
		}else if("Nixdorf".equals(ClientAppSettings.GetString(settingsId))){
                    return new PosPrintNixdorf();
                } else if("Ostalo 32".equals(ClientAppSettings.GetString(settingsId))){
			return new PosPrintJobOther(32);
		} else if("Ostalo 42".equals(ClientAppSettings.GetString(settingsId))){
			return new PosPrintJobOther(42);
		} else if("Ostalo 56".equals(ClientAppSettings.GetString(settingsId))){
			return new PosPrintJobOther(56);
		} else {
			return new PosPrintJobOther(42);
		}
	}
        
        public static String CalculateExchangeRate(float amount)
        {
            String temp = null;
            float rez = amount / PrintUtils.ExchangeRate;
            temp = String.format("%.2f", rez);
            return temp;
        }
        public static float floatCalculateExchangeRate(float amount)
        {
            String temp = null;
            float rez = amount / PrintUtils.ExchangeRate;
            temp = String.format("%.2f", rez);
            return rez;
        }
        public static String EurToKn(float amount){
            String temp = null;
            float rez = amount * PrintUtils.ExchangeRate;
            temp = String.format("%.2f", rez);
            return temp;
        }
        
        public static float floatEurToKn(float amount)
        {
            String temp = null;
            float rez = amount * PrintUtils.ExchangeRate;
            temp = String.format("%.2f", rez);
            return rez;
        }
}
