package cn.cvzhanshi;

import cn.cvzhanshi.entity.User;
import com.alibaba.fastjson.JSON;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.FetchSourceContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.sql.SQLOutput;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@SpringBootTest
class EsApiApplicationTests {

	@Autowired
	@Qualifier("restHighLevelClient")
	private RestHighLevelClient client;
	//测试索引的创建
	@Test
	public void testCreateIndex() throws IOException {
		// 1、创建索引请求
		CreateIndexRequest request = new CreateIndexRequest("cvzhanshi_index");
		// 2、客户端执行请求 IndicesClient, 请求后获得响应
		CreateIndexResponse createIndexResponse = client.indices().create(request, RequestOptions.DEFAULT);
		System.out.println(createIndexResponse);
	}

	//测试获取索引
	@Test
	void testExistIndex() throws IOException{
		GetIndexRequest request = new GetIndexRequest("cvzhanshi_index1");
		boolean exists = client.indices().exists(request, RequestOptions.DEFAULT);
		System.out.println(exists);
	}

	// 测试删除索引
	@Test
	void testDeleteIndex() throws IOException {
		DeleteIndexRequest request = new DeleteIndexRequest("cvzhanshi_index");
		//删除
		AcknowledgedResponse delete = client.indices().delete(request, RequestOptions.DEFAULT);
		System.out.println(delete.isAcknowledged());

	}

	//===============================文档api测试===============================

	//测试文档的添加
	@Test
	void testAddDocument() throws IOException {
		//创建对象
		User user = new User("cvzhanshi", 20);
		//创建请求
		IndexRequest request = new IndexRequest("cvzhanshi_index");
		//规则 put /kuang_index/_doc/1
		request.id("1");
		//设置请求超时时间 1s
		request.timeout(TimeValue.timeValueSeconds(1));
		// 将我们的数据放入请求  json
		request.source(JSON.toJSONString(user), XContentType.JSON);
		// 客户端发送请求 , 获取响应的结果
		IndexResponse indexResponse  = client.index(request, RequestOptions.DEFAULT);

		System.out.println(indexResponse.toString());
		System.out.println(indexResponse.status());
	}

	//判断文档是否存在
	@Test
	void testIsExists() throws IOException {
		GetRequest getRequest = new GetRequest("cvzhanshi_index", "1");
		// 不获取返回的 _source 的上下文了
		getRequest.fetchSourceContext(new FetchSourceContext(false));
		getRequest.storedFields("_none_");

		boolean exists = client.exists(getRequest, RequestOptions.DEFAULT);
		System.out.println(exists);

	}

	//获取文档信息
	@Test
	void testGetDocument() throws IOException {
		GetRequest getRequest = new GetRequest("cvzhanshi_index", "1");
		// 不获取返回的 _source 的上下文了
//		getRequest.fetchSourceContext(new FetchSourceContext(false));
//		getRequest.storedFields("_none_");
		GetResponse getResponse = client.get(getRequest, RequestOptions.DEFAULT);
		System.out.println(getResponse);
		System.out.println(getResponse.getSourceAsString());
	}

	//更新文档信息
	@Test
	void testUpdateDocument() throws IOException {
		UpdateRequest request = new UpdateRequest("cvzhanshi_index","1");
		request.timeout("1s");

		User user = new User("cv战士", 21);
		request.doc(JSON.toJSONString(user),XContentType.JSON);

		UpdateResponse update = client.update(request, RequestOptions.DEFAULT);
		System.out.println(update.status());
	}
	// 删除文档记录
	@Test
	void testDeleteRequest() throws IOException {
		DeleteRequest deleteRequest = new DeleteRequest("cvzhanshi_index", "1");
		deleteRequest.timeout("1s");
		DeleteResponse deleteResponse = client.delete(deleteRequest, RequestOptions.DEFAULT);
		System.out.println(deleteResponse.status());
	}


	// 特殊的，真的项目一般都会批量插入数据！
	@Test
	void testBulkRequest() throws IOException {
		BulkRequest bulkRequest = new BulkRequest();
		bulkRequest.timeout("10s");

		List<User> userList = new ArrayList<>();
		userList.add(new User("cvzhanshi",20));
		userList.add(new User("user",21));
		userList.add(new User("ursula",22));
		userList.add(new User("wile",23));
		userList.add(new User("CVZHANSHI",24));

		for (int i = 0;i< userList.size();i++){
			bulkRequest.add(
					new IndexRequest("cvzhanshi_index")
					.id("" + (i+1))
					.source(JSON.toJSONString(userList.get(i)),XContentType.JSON)
			);
		}
		BulkResponse bulk = client.bulk(bulkRequest, RequestOptions.DEFAULT);
		System.out.println(bulk.hasFailures());//false 返回是否失败，false表示成功
	}

	// 查询
	// SearchRequest 搜索请求
	// SearchSourceBuilder 条件构造
	//  HighlightBuilder 构建高亮
	//  TermQueryBuilder 精确查询
	//  MatchAllQueryBuilder
	//  xxx QueryBuilder 对应我们刚才看到的命令！
	@Test
	void testSearch() throws IOException {
		SearchRequest searchRequest = new SearchRequest("cvzhanshi_index");
		//构建搜索条件
		SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();

		//查询条件,我们可以使用SearchSourceBuilder工具来实现
		//精确匹配QueryBuilders.termQuery()
		TermQueryBuilder termQueryBuilder = QueryBuilders.termQuery("name", "cvzhanshi");
		sourceBuilder.query(termQueryBuilder);
		sourceBuilder.timeout(new TimeValue(60, TimeUnit.SECONDS));

		searchRequest.source(sourceBuilder);

		SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
		System.out.println(JSON.toJSONString(searchResponse));
		System.out.println("+++++++++++++++++++++++++++++++++++++++++");
		for (SearchHit hit : searchResponse.getHits()) {
			System.out.println(hit.getSourceAsMap());
		}
	}
}
