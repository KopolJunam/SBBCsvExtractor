package ch.ierax.sbbcsvextractor;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

public class Main {
    private static final DateTimeFormatter DATEFORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private static final String TICKETTYPEFILENAME = "./tickettypes.txt";
    private static final List<String> originalTicketTypes = List.of(" ", "Arcobaleno Einzelbillett", "Klassenwechsel Strecke"
            , "Sparbillett", "Sparklassenwechsel", "Spartageskarte", "Streckenbillett", "Z-Pass OSTWIND-ZVV " +
                    "Tageskarte", "ZVV Einzelbillett", "engadin mobil Einzelbillett");

    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Es muss ein Parameter mit dem PDF übergeben werden.");
            return;
        }
        File file = new File(args[0]);
        try {
            PDDocument document = PDDocument.load(file);
            PDFTextStripper stripper = new PDFTextStripper();

            int lineNo = 100;
            String line;

            List<String> travellers = null;
            TicketInfo ticketInfo = null;
            LocalDate travellingDate = null;
            OrderData1 orderData1 = null;
            OrderData2 orderData2 = null;
            List<String> unknownTicketTypes = new ArrayList<>();
            List<String> ticketTypes = new ArrayList<>();

            writeTicketTypeFileIfNecessary();
            readTicketTypeFile(ticketTypes);

            BufferedReader reader = new BufferedReader(new StringReader(stripper.getText(document)));
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("Reisende")) {
                    lineNo = 0;
                }
                switch (lineNo) {
                    case 0 -> {
                        if (travellers == null) {
                            System.out.println("Reisedatum;Billett-Typ;Von;Nach;Preis;Reisende(r);Bestelldatum;" +
                                    "Lieferadresse;Bestellnummer;Zahlungsmittel");
                        } else {
                            for (String traveller : travellers) {
                                System.out.println(travellingDate.format(DATEFORMATTER) + ";" +
                                        ticketInfo.ticketType() + ";" + ticketInfo.from() + ";" +
                                        ticketInfo.to() + ";" +
                                        String.format("%.2f", (ticketInfo.price() / (double) (travellers.size()))) +
                                        ";" + traveller + ";" + orderData1.orderingDate().format(DATEFORMATTER) + ";" +
                                        orderData1.deliveryAddress() + ";" + orderData2.orderNo() + ";" +
                                        orderData2.paymentMethod());
                            }
                        }
                        travellers = parseTravellers(line);
                    }
                    case 1 -> {
                        try {
                            ticketInfo = parseTicketInfo(ticketTypes, line);
                        } catch (TicketTypeException e) {
                            unknownTicketTypes.add(e.getMessage());
                            lineNo = 4;
                        }
                    }
                    case 2 -> travellingDate = parseTravellingDate(line);
                    case 3 -> orderData1 = parseOrderData1(line);
                    case 4 -> orderData2 = parseOrderData2(line);
                }
                lineNo++;
            }
            if (!unknownTicketTypes.isEmpty()) {
                System.out.println("Es hatte folgende Zeilen mit unbekannten Ticket-Typen. Bitte in ./tickettypes" +
                        ".txt" + " ergänzen:");
                unknownTicketTypes.stream().distinct().sorted().forEach(System.out::println);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    private static List<String> parseTravellers(String line) {
        return Arrays.stream(line.split(" ", 2)[1].split(",")).map(String::trim).collect(Collectors.toList());
    }

    private static TicketInfo parseTicketInfo(List<String> ticketTypes, String line) throws TicketTypeException {
        String ticketType = null;

        if (line.startsWith(" ")) {
            ticketType = "International";
        } else {
            for (String type : ticketTypes) {
                if (line.startsWith(type)) {
                    ticketType = type;
                    line = line.substring(ticketType.length() + 1);
                    break;
                }
            }
        }
        if (ticketType == null) {
            throw new TicketTypeException(line);
        }
        String[] tokens = line.split(" ");
        double price = Double.parseDouble(tokens[tokens.length - 2]);
        String itinerary = "";
        for (int i = 0; i < tokens.length - 2; i++) {
            itinerary += tokens[i] + " ";
        }

        String[] itineraryTokens = itinerary.split("-");
        String from = itineraryTokens[0].trim();
        String to = itineraryTokens.length > 1 ? itineraryTokens[1].trim() : "";

        return new TicketInfo(ticketType, from, to, price);
    }

    private static LocalDate parseTravellingDate(String line) {
        return LocalDate.parse(line.split(" ")[1].trim(), DATEFORMATTER);
    }

    private static OrderData1 parseOrderData1(String line) {
        String[] tokens = line.split(" ");
        return new OrderData1(tokens[1].trim(), LocalDate.parse(tokens[3].trim(), DATEFORMATTER));
    }

    private static OrderData2 parseOrderData2(String line) {
        String[] tokens = line.split("Bestellnummer:");
        if (tokens[0].isBlank()) {
            tokens[0] = "Zahlungsart: Unbekannt";
        }
        return new OrderData2(tokens[0].split(" ", 2)[1].trim(), tokens[1].trim());
    }

    private static void writeTicketTypeFileIfNecessary() throws IOException {
        File typeFile = new File(TICKETTYPEFILENAME);
        if (!typeFile.exists()) {
            try (BufferedWriter bw =
                         new BufferedWriter(new OutputStreamWriter(new FileOutputStream(typeFile)))) {
                for (String line : originalTicketTypes) {
                    if (!line.startsWith(" ")) {
                        bw.write(line);
                        bw.newLine();
                    }
                }
            }
        }
    }

    private static void readTicketTypeFile(List<String> ticketTypes) throws IOException {
        File typeFile = new File(TICKETTYPEFILENAME);
        Scanner FileReader = new Scanner(typeFile);
        while (FileReader.hasNextLine()) {
            ticketTypes.add(FileReader.nextLine());
        }
    }

    private static class TicketTypeException extends Exception {
        public TicketTypeException(String message) {
            super(message);
        }
    }

    private record TicketInfo(String ticketType, String from, String to, double price) {
    }

    private record OrderData1(String deliveryAddress, LocalDate orderingDate) {
    }

    private record OrderData2(String paymentMethod, String orderNo) {
    }
}