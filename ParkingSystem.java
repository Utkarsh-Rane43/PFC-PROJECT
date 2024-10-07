import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
public class ParkingSystem extends JFrame {
    private static final String FILE_NAME = "parking_data.txt";
    private static final int TOTAL_SLOTS = 10;
    private Map<String, CarDetails> parkedCars = new HashMap<>();
    private JButton[] slotButtons = new JButton[TOTAL_SLOTS];
    private List<Integer> availableSlots = new ArrayList<>();
    private DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private JTextField carNumberField, inTimeField, outTimeField;
    private JButton parkButton, unparkButton;
    private JTextArea displayArea;
    public ParkingSystem() {
        setTitle("Beginner Parking System");
        setSize(600, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(null);
        JLabel backgroundLabel = new JLabel();
        backgroundLabel.setBounds(0, 0, 600, 500);
        add(backgroundLabel);
        JLabel carNumberLabel = new JLabel("Car Number:");
        carNumberLabel.setBounds(20, 20, 100, 30);
        add(carNumberLabel);
        carNumberField = new JTextField();
        carNumberField.setBounds(120, 20, 200, 30);
        add(carNumberField);
        JLabel inTimeLabel = new JLabel("In Time (yyyy-MM-dd HH:mm:ss):");
        inTimeLabel.setBounds(20, 60, 220, 30);
        add(inTimeLabel);
        inTimeField = new JTextField();
        inTimeField.setBounds(240, 60, 200, 30);
        add(inTimeField);
        JLabel outTimeLabel = new JLabel("Out Time (yyyy-MM-dd HH:mm:ss):");
        outTimeLabel.setBounds(20, 100, 220, 30);
        add(outTimeLabel);
        outTimeField = new JTextField();
        outTimeField.setBounds(240, 100, 200, 30);
        add(outTimeField);
        for (int i = 0; i < TOTAL_SLOTS; i++) {
            slotButtons[i] = new JButton("Slot " + (i + 1));
            slotButtons[i].setBounds(20 + (i % 5) * 110, 140 + (i / 5) * 60, 100, 50);
            slotButtons[i].setBackground(Color.GREEN);
            add(slotButtons[i]);
        }
        parkButton = new JButton("Park Car");
        parkButton.setBounds(20, 300, 100, 30);
        add(parkButton);
        unparkButton = new JButton("Unpark Car");
        unparkButton.setBounds(140, 300, 100, 30);
        add(unparkButton);
        displayArea = new JTextArea();
        displayArea.setBounds(20, 340, 550, 120);
        displayArea.setEditable(false);
        add(displayArea);
        for (int i = 1; i <= TOTAL_SLOTS; i++) {
            availableSlots.add(i);
        }
        loadParkingData();
        parkButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                parkCar();
            }
        });
        unparkButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                unparkCar();
            }
        });
        setVisible(true);
    }
    private void parkCar() {
        String carNumber = carNumberField.getText().trim();
        String inTime = inTimeField.getText().trim();
        if (carNumber.isEmpty() || inTime.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter car number and in time.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        try {
            LocalDateTime.parse(inTime, timeFormatter);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Invalid in time format. Use yyyy-MM-dd HH:mm:ss", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (parkedCars.containsKey(carNumber)) {
            JOptionPane.showMessageDialog(this, "Car is already parked.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (availableSlots.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Parking full. No available slots.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        int slot = availableSlots.remove(0);  
        slotButtons[slot - 1].setBackground(Color.RED); 
        CarDetails carDetails = new CarDetails(carNumber, slot, inTime);
        parkedCars.put(carNumber, carDetails);
        saveParkingData();
        displayArea.append("Car parked at slot " + slot + "\n");
        carNumberField.setText("");
        inTimeField.setText("");
    }
    private void unparkCar() {
        String carNumber = carNumberField.getText().trim();
        String outTime = outTimeField.getText().trim();
        if (carNumber.isEmpty() || outTime.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter car number and out time.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (!parkedCars.containsKey(carNumber)) {
            JOptionPane.showMessageDialog(this, "Car not found in parking.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        try {
            LocalDateTime.parse(outTime, timeFormatter);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Invalid out time format. Use yyyy-MM-dd HH:mm:ss", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        CarDetails carDetails = parkedCars.get(carNumber);
        LocalDateTime inTime = LocalDateTime.parse(carDetails.getInTime(), timeFormatter);
        LocalDateTime unparkTime = LocalDateTime.parse(outTime, timeFormatter);
        Duration duration = Duration.between(inTime, unparkTime);
        double charge = calculateCharge(duration);
        slotButtons[carDetails.getSlot() - 1].setBackground(Color.GREEN);
        availableSlots.add(carDetails.getSlot());
        parkedCars.remove(carNumber);
        saveParkingData();
        String receipt = "Receipt:\nCar Number: " + carDetails.getCarNumber() + "\nSlot: " + carDetails.getSlot() +
                "\nIn Time: " + carDetails.getInTime() + "\nOut Time: " + outTime +
                "\nTotal Charge: Rs. " + charge;
        JOptionPane.showMessageDialog(this, receipt, "Receipt", JOptionPane.INFORMATION_MESSAGE);
        displayArea.append("Car unparked from slot " + carDetails.getSlot() + ". Total charge: Rs. " + charge + "\n");
        carNumberField.setText("");
        outTimeField.setText("");
    }
    private double calculateCharge(Duration duration) {
        long totalMinutes = duration.toMinutes();
        double charge = 10;
        if (totalMinutes > 120) {
            long extraMinutes = totalMinutes - 120;
            charge += (extraMinutes / 30) * 5;
        }
        return charge;
    }
    private void loadParkingData() {
        try (BufferedReader reader = new BufferedReader(new FileReader(FILE_NAME))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                String carNumber = parts[0];
                int slot = Integer.parseInt(parts[1]);
                String inTime = parts[2];
                parkedCars.put(carNumber, new CarDetails(carNumber, slot, inTime));
                availableSlots.remove(Integer.valueOf(slot));
                slotButtons[slot - 1].setBackground(Color.RED);
            }
        } catch (FileNotFoundException e) {
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error loading parking data.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    private void saveParkingData() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_NAME))) {
            for (CarDetails carDetails : parkedCars.values()) {
                writer.write(carDetails.getCarNumber() + "," + carDetails.getSlot() + "," + carDetails.getInTime());
                writer.newLine();
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error saving parking data.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    class CarDetails {
        private String carNumber;
        private int slot;
        private String inTime;
        public CarDetails(String carNumber, int slot, String inTime) {
            this.carNumber = carNumber;
            this.slot = slot;
            this.inTime = inTime;
        }
        public String getCarNumber() {
            return carNumber;
        }
        public int getSlot() {
            return slot;
        }
        public String getInTime() {
            return inTime;
        }
    }
    public static void main(String[] args) {
        new ParkingSystem();
    }
}
