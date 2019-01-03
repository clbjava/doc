-------------------------------------------------------------------------------------------------------------------------------------------
Read a file line by line
-------------------------------------------------------------------------------------------------------------------------------------------
Read File + Stream
-------------------------------------------------------------------------------------------------------------------------------------------
    String fileName = "c://lines.txt";
		//read file into stream, try-with-resources
		try (Stream<String> stream = Files.lines(Paths.get(fileName))) {
			stream.forEach(System.out::println);
		} catch (IOException e) {
			e.printStackTrace();
		}
-------------------------------------------------------------------------------------------------------------------------------------------
Read File + Stream + Extra
-------------------------------------------------------------------------------------------------------------------------------------------
String fileName = "c://lines.txt";
		List<String> list = new ArrayList<>();

		try (Stream<String> stream = Files.lines(Paths.get(fileName))) {

			//1. filter line 3
			//2. convert all content to upper case
			//3. convert it into a List
			list = stream
					.filter(line -> !line.startsWith("line3"))
					.map(String::toUpperCase)
					.collect(Collectors.toList());

		} catch (IOException e) {
			e.printStackTrace();
		}

		list.forEach(System.out::println);
-------------------------------------------------------------------------------------------------------------------------------------------
BufferedReader + Stream
-------------------------------------------------------------------------------------------------------------------------------------------
String fileName = "c://lines.txt";
		List<String> list = new ArrayList<>();

		try (BufferedReader br = Files.newBufferedReader(Paths.get(fileName))) {

			//br returns as stream and convert it into a List
			list = br.lines().collect(Collectors.toList());

		} catch (IOException e) {
			e.printStackTrace();
		}
   list.forEach(System.out::println);
-------------------------------------------------------------------------------------------------------------------------------------------
BufferedReader And Scanner
-------------------------------------------------------------------------------------------------------------------------------------------
	String fileName = "c://lines.txt";

		try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {

			String line;
			while ((line = br.readLine()) != null) {
				System.out.println(line);
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
-------------------------------------------------------------------------------------------------------------------------------------------
 try-with-resources 
 -------------------------------------------------------------------------------------------------------------------------------------------
	String fileName = "c://lines.txt";

		try (Scanner scanner = new Scanner(new File(fileName))) {

			while (scanner.hasNext()){
				System.out.println(scanner.nextLine());
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
-------------------------------------------------------------------------------------------------------------------------------------------
-------------------------------------------------------------------------------------------------------------------------------------------
BufferedWriter
-------------------------------------------------------------------------------------------------------------------------------------------
//Get the file reference
Path path = Paths.get("c:/output.txt");
 
//Use try-with-resource to get auto-closeable writer instance
try (BufferedWriter writer = Files.newBufferedWriter(path))
{
    writer.write("Hello World !!");
}

-------------------------------------------------------------------------------------------------------------------------------------------












