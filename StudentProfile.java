import java.util.Objects;

/** Biyografide gösterilecek verileri tutar; ağ işlemi yapmaz. */
public final class StudentProfile {
    private final String fullName;
    private final String studentNumber;
    private final String biography;

    public StudentProfile(String fullName, String studentNumber, String biography) {
        this.fullName = Objects.requireNonNull(fullName);
        this.studentNumber = Objects.requireNonNull(studentNumber);
        this.biography = Objects.requireNonNull(biography);
    }

    public String getFullName() { return fullName; }
    public String getStudentNumber() { return studentNumber; }
    public String getBiography() { return biography; }
}
