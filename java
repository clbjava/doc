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

-------------------------------------------------------NIO------------------------------------------------------------------------------------
NIO 2019-01-14

Selector会不断地轮询注册在其上的channel,如果某个channel上面发生读或者写时间,这个Channel就处于就绪状态,会被selector轮询出来,
然后通过SelectionKey可以获取就绪的Channel的集合,然后进行后续的IO操作（阻塞）--》

传统的BIO里面socket.read()，如果TCP RecvBuffer里没有数据，函数会一直阻塞，直到收到数据，返回读到的数据。
对于NIO，如果TCP RecvBuffer有数据，就把数据从网卡读到内存，并且返回给用户；反之则直接返回0，永远不会阻塞。
最新的AIO(Async I/O)里面会更进一步：不但等待就绪是非阻塞的，就连数据从网卡到内存的过程也是异步的。
换句话说，BIO里用户最关心“我要读”，NIO里用户最关心”我可以读了”，在AIO模型里用户更需要关注的是“读完了”。
NIO一个重要的特点是：socket主要的读、写、注册和接收函数，在等待就绪阶段都是非阻塞的，真正的I/O操作是同步阻塞的（消耗CPU但性能非常高）。

-------------------------------------------------------------------------------------------------------------------------------------
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

public class Server implements Runnable{
	//1 多路复用器（管理所有的通道）
	private Selector seletor;
	//2 建立缓冲区
	private ByteBuffer readBuf = ByteBuffer.allocate(1024);
	//3 
	private ByteBuffer writeBuf = ByteBuffer.allocate(1024);
	public Server(int port){
		try {
			//1 打开路复用器
			this.seletor = Selector.open();
			//2 打开服务器通道
			ServerSocketChannel ssc = ServerSocketChannel.open();
			//3 设置服务器通道为非阻塞模式
			ssc.configureBlocking(false);
			//4 绑定地址
			ssc.bind(new InetSocketAddress(port));
			//5 把服务器通道注册到多路复用器上，并且监听阻塞事件
			ssc.register(this.seletor, SelectionKey.OP_ACCEPT);
			
			System.out.println("Server start, port :" + port);
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		while(true){
			try {
				//1 必须要让多路复用器开始监听
				this.seletor.select();
				//2 返回多路复用器已经选择的结果集
				Iterator<SelectionKey> keys = this.seletor.selectedKeys().iterator();
				//3 进行遍历
				while(keys.hasNext()){
					//4 获取一个选择的元素
					SelectionKey key = keys.next();
					//5 直接从容器中移除就可以了
					keys.remove();
					//6 如果是有效的
					if(key.isValid()){
						//7 如果为阻塞状态
						if(key.isAcceptable()){
							this.accept(key);
						}
						//8 如果为可读状态
						if(key.isReadable()){
							this.read(key);
						}
						//9 写数据
						if(key.isWritable()){
							//this.write(key); //ssc
						}
					}
					
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	private void write(SelectionKey key){
		//ServerSocketChannel ssc =  (ServerSocketChannel) key.channel();
		//ssc.register(this.seletor, SelectionKey.OP_WRITE);
	}

	private void read(SelectionKey key) {
		try {
			//1 清空缓冲区旧的数据
			this.readBuf.clear();
			//2 获取之前注册的socket通道对象
			SocketChannel sc = (SocketChannel) key.channel();
			//3 读取数据
			int count = sc.read(this.readBuf);
			//4 如果没有数据
			if(count == -1){
				key.channel().close();
				key.cancel();
				return;
			}
			//5 有数据则进行读取 读取之前需要进行复位方法(把position 和limit进行复位)
			this.readBuf.flip();
			//6 根据缓冲区的数据长度创建相应大小的byte数组，接收缓冲区的数据
			byte[] bytes = new byte[this.readBuf.remaining()];
			//7 接收缓冲区数据
			this.readBuf.get(bytes);
			//8 打印结果
			String body = new String(bytes).trim();
			System.out.println("Server : " + body);
			
			// 9..可以写回给客户端数据 
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

	private void accept(SelectionKey key) {
		try {
			//1 获取服务通道
			ServerSocketChannel ssc =  (ServerSocketChannel) key.channel();
			//2 执行阻塞方法
			SocketChannel sc = ssc.accept();
			//3 设置阻塞模式
			sc.configureBlocking(false);
			//4 注册到多路复用器上，并设置读取标识
			sc.register(this.seletor, SelectionKey.OP_READ);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		
		new Thread(new Server(8765)).start();;
	}
	
	
}
客户端

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class Client {

	//需要一个Selector 
	public static void main(String[] args) {
		
		//创建连接的地址
		InetSocketAddress address = new InetSocketAddress("127.0.0.1", 8765);
		
		//声明连接通道
		SocketChannel sc = null;
		
		//建立缓冲区
		ByteBuffer buf = ByteBuffer.allocate(1024);
		
		try {
			//打开通道
			sc = SocketChannel.open();
			//进行连接
			sc.connect(address);
			
			while(true){
				//定义一个字节数组，然后使用系统录入功能：
				byte[] bytes = new byte[1024];
				System.in.read(bytes);
				
				//把数据放到缓冲区中
				buf.put(bytes);
				//对缓冲区进行复位
				buf.flip();
				//写出数据
				sc.write(buf);
				//清空缓冲区数据
				buf.clear();
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if(sc != null){
				try {
					sc.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
	}
	
}

-------------------------------------------------------------------------------------------------------------------------------------
-------------------------------------------------------------------------------------------------------------------------------------










