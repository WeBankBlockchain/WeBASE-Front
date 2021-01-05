package com.webank.webase.front.contractStore;

import com.webank.webase.front.contract.entity.Contract;
import com.webank.webase.front.contractStore.ContractFolderRepository;
import com.webank.webase.front.contractStore.ContractItemRepository;
import com.webank.webase.front.contractStore.ContractStoreRepository;
import com.webank.webase.front.contractStore.entity.ContractFolderItem;
import com.webank.webase.front.contractStore.entity.ContractItem;
import com.webank.webase.front.contractStore.entity.StoreItem;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class PresetDataService {

    @Autowired
    private ContractItemRepository contractItemRepository;
    @Autowired
    private ContractFolderRepository contractFolderRepository;
    @Autowired
    private ContractStoreRepository contractStoreRepository;


    public void initStoreItem() {
        insertStoreItem(1,"工具箱","Toolbox","1","1",
                "工具箱中有常用的工具合约","工具箱中有常用的工具合约",
                "There are common tool contracts in the toolbox","There are common tool contracts in the toolbox");

        insertStoreItem(2,"存证应用","Evidence","2","1",
                "一套区块链存证合约","一套区块链存证合约",
                "A set of contracts for the preservation of evidence","A set of contracts for the preservation of evidence");

        insertStoreItem(3,"存证应用","Evidence","2","1",
                "一套区块链存证合约","一套区块链存证合约",
                "A set of contracts for the preservation of evidence","A set of contracts for the preservation of evidence");
    }

    public void insertStoreItem(long storeId, String storeName, String StoreName_en, String StoreType,
                                String StoreIcon, String StoreDesc, String StoreDetail,String StoreDesc_en,
                                String StoreDetail_en)
    {
        StoreItem storeItem = new StoreItem();
        storeItem.setStoreId(storeId);
        storeItem.setStoreName(storeName);
        storeItem.setStoreName_en(StoreName_en);
        storeItem.setStoreType(StoreType);
        storeItem.setStoreIcon(StoreIcon);
        storeItem.setStoreDesc(StoreDesc);
        storeItem.setStoreDetail(StoreDetail);
        storeItem.setStoreDesc_en(StoreDesc_en);
        storeItem.setStoreDetail_en(StoreDetail_en);
        storeItem.setCreateTime(LocalDateTime.now());
        storeItem.setModifyTime(storeItem.getCreateTime());
        contractStoreRepository.save(storeItem);
    }

    public void initContractFolderItem() {
        insertContractFolderItem(1,1,"tools","tools",
                "tools","","");

        insertContractFolderItem(2,2,"evidence","evidence",
                "evidence","","");

        insertContractFolderItem(3,3,"address","address",
                "address","","");
    }

    public void insertContractFolderItem(long contractFolderId, long storeId, String contractFolderName, String contractFolderDesc, String contractFolderDetail,
                                         String contractFolderDesc_en, String contractFolderDetail_en)
    {
        ContractFolderItem contractFolderItem = new ContractFolderItem();
        contractFolderItem.setContractFolderId(contractFolderId);
        contractFolderItem.setStoreId(storeId);
        contractFolderItem.setContractFolderName(contractFolderName);
        contractFolderItem.setContractFolderDesc(contractFolderDesc);
        contractFolderItem.setContractFolderDetail(contractFolderDetail);
        contractFolderItem.setContractFolderDesc_en(contractFolderDesc_en);
        contractFolderItem.setContractFolderDetail_en(contractFolderDetail_en);
        contractFolderItem.setCreateTime(LocalDateTime.now());
        contractFolderItem.setModifyTime(contractFolderItem.getCreateTime());
        contractFolderRepository.save(contractFolderItem);

    }

    public void initContractItem() {

        insertContractItem(1,1,"LibString","LyoKICogQ29weXJpZ2h0IDIwMTQtMjAxOSB0aGUgb3JpZ2luYWwgYXV0aG9yIG9yIGF1dGhvcnMuCiAqCiAqIExpY2Vuc2VkIHVuZGVyIHRoZSBBcGFjaGUgTGljZW5zZSwgVmVyc2lvbiAyLjAgKHRoZSAiTGljZW5zZSIpOwogKiB5b3UgbWF5IG5vdCB1c2UgdGhpcyBmaWxlIGV4Y2VwdCBpbiBjb21wbGlhbmNlIHdpdGggdGhlIExpY2Vuc2UuCiAqIFlvdSBtYXkgb2J0YWluIGEgY29weSBvZiB0aGUgTGljZW5zZSBhdAogKgogKiAgICAgIGh0dHA6Ly93d3cuYXBhY2hlLm9yZy9saWNlbnNlcy9MSUNFTlNFLTIuMAogKgogKiBVbmxlc3MgcmVxdWlyZWQgYnkgYXBwbGljYWJsZSBsYXcgb3IgYWdyZWVkIHRvIGluIHdyaXRpbmcsIHNvZnR3YXJlCiAqIGRpc3RyaWJ1dGVkIHVuZGVyIHRoZSBMaWNlbnNlIGlzIGRpc3RyaWJ1dGVkIG9uIGFuICJBUyBJUyIgQkFTSVMsCiAqIFdJVEhPVVQgV0FSUkFOVElFUyBPUiBDT05ESVRJT05TIE9GIEFOWSBLSU5ELCBlaXRoZXIgZXhwcmVzcyBvciBpbXBsaWVkLgogKiBTZWUgdGhlIExpY2Vuc2UgZm9yIHRoZSBzcGVjaWZpYyBsYW5ndWFnZSBnb3Zlcm5pbmcgcGVybWlzc2lvbnMgYW5kCiAqIGxpbWl0YXRpb25zIHVuZGVyIHRoZSBMaWNlbnNlLgogKiAqLwoKcHJhZ21hIHNvbGlkaXR5IF4wLjQuMjU7CgpsaWJyYXJ5IExpYkFkZHJlc3N7CiAgICAKICAgIGZ1bmN0aW9uIGlzQ29udHJhY3QoYWRkcmVzcyBhY2NvdW50KSBpbnRlcm5hbCB2aWV3IHJldHVybnMoYm9vbCkgewogICAgICAgIHVpbnQyNTYgc2l6ZTsKICAgICAgICBhc3NlbWJseSB7IHNpemUgOj0gZXh0Y29kZXNpemUoYWRkcikgfSAgCiAgICAgICAgcmV0dXJuIHNpemUgPiAwOwogICAgfQoKICAgIGZ1bmN0aW9uIGlzRW1wdHlBZGRyZXNzKGFkZHJlc3MgYWRkcikgaW50ZXJuYWwgcHVyZSByZXR1cm5zKGJvb2wpewogICAgICAgIHJldHVybiBhZGRyID09IGFkZHJlc3MoMCk7CiAgICB9Cn0K",
                "LibString","LibString");
        insertContractItem(2,1,"LibSafeMathForUint256Utils","LyoKICogQ29weXJpZ2h0IDIwMTQtMjAxOSB0aGUgb3JpZ2luYWwgYXV0aG9yIG9yIGF1dGhvcnMuCiAqCiAqIExpY2Vuc2VkIHVuZGVyIHRoZSBBcGFjaGUgTGljZW5zZSwgVmVyc2lvbiAyLjAgKHRoZSAiTGljZW5zZSIpOwogKiB5b3UgbWF5IG5vdCB1c2UgdGhpcyBmaWxlIGV4Y2VwdCBpbiBjb21wbGlhbmNlIHdpdGggdGhlIExpY2Vuc2UuCiAqIFlvdSBtYXkgb2J0YWluIGEgY29weSBvZiB0aGUgTGljZW5zZSBhdAogKgogKiAgICAgIGh0dHA6Ly93d3cuYXBhY2hlLm9yZy9saWNlbnNlcy9MSUNFTlNFLTIuMAogKgogKiBVbmxlc3MgcmVxdWlyZWQgYnkgYXBwbGljYWJsZSBsYXcgb3IgYWdyZWVkIHRvIGluIHdyaXRpbmcsIHNvZnR3YXJlCiAqIGRpc3RyaWJ1dGVkIHVuZGVyIHRoZSBMaWNlbnNlIGlzIGRpc3RyaWJ1dGVkIG9uIGFuICJBUyBJUyIgQkFTSVMsCiAqIFdJVEhPVVQgV0FSUkFOVElFUyBPUiBDT05ESVRJT05TIE9GIEFOWSBLSU5ELCBlaXRoZXIgZXhwcmVzcyBvciBpbXBsaWVkLgogKiBTZWUgdGhlIExpY2Vuc2UgZm9yIHRoZSBzcGVjaWZpYyBsYW5ndWFnZSBnb3Zlcm5pbmcgcGVybWlzc2lvbnMgYW5kCiAqIGxpbWl0YXRpb25zIHVuZGVyIHRoZSBMaWNlbnNlLgogKiAqLwoKcHJhZ21hIHNvbGlkaXR5IF4wLjQuMjU7CgpsaWJyYXJ5IExpYlNhZmVNYXRoRm9yVWludDI1NlV0aWxzIHsKCiAgICBmdW5jdGlvbiBhZGQodWludDI1NiBhLCB1aW50MjU2IGIpIGludGVybmFsIHB1cmUgcmV0dXJucyAodWludDI1NikgewogICAgICAgIHVpbnQyNTYgYyA9IGEgKyBiOwogICAgICAgIHJlcXVpcmUoYyA+PSBhLCAiU2FmZU1hdGhGb3JVaW50MjU2OiBhZGRpdGlvbiBvdmVyZmxvdyIpOwogICAgICAgIHJldHVybiBjOwogICAgfQoKICAgIGZ1bmN0aW9uIHN1Yih1aW50MjU2IGEsIHVpbnQyNTYgYikgaW50ZXJuYWwgcHVyZSByZXR1cm5zICh1aW50MjU2KSB7CiAgICAgICAgcmVxdWlyZShiIDw9IGEsICJTYWZlTWF0aEZvclVpbnQyNTY6IHN1YnRyYWN0aW9uIG92ZXJmbG93Iik7CiAgICAgICAgdWludDI1NiBjID0gYSAtIGI7CiAgICAgICAgcmV0dXJuIGM7CiAgICB9CgogICAgZnVuY3Rpb24gbXVsKHVpbnQyNTYgYSwgdWludDI1NiBiKSBpbnRlcm5hbCBwdXJlIHJldHVybnMgKHVpbnQyNTYpIHsKICAgICAgICBpZiAoYSA9PSAwIHx8IGIgPT0gMCkgewogICAgICAgICAgICByZXR1cm4gMDsKICAgICAgICB9CgogICAgICAgIHVpbnQyNTYgYyA9IGEgKiBiOwogICAgICAgIHJlcXVpcmUoYyAvIGEgPT0gYiwgIlNhZmVNYXRoRm9yVWludDI1NjogbXVsdGlwbGljYXRpb24gb3ZlcmZsb3ciKTsKICAgICAgICByZXR1cm4gYzsKICAgIH0KCiAgICBmdW5jdGlvbiBkaXYodWludDI1NiBhLCB1aW50MjU2IGIpIGludGVybmFsIHB1cmUgcmV0dXJucyAodWludDI1NikgewogICAgICAgIHJlcXVpcmUoYiA+IDAsICJTYWZlTWF0aEZvclVpbnQyNTY6IGRpdmlzaW9uIGJ5IHplcm8iKTsKICAgICAgICB1aW50MjU2IGMgPSBhIC8gYjsKICAgICAgICByZXR1cm4gYzsKICAgIH0KCiAgICBmdW5jdGlvbiBtb2QodWludDI1NiBhLCB1aW50MjU2IGIpIGludGVybmFsIHB1cmUgcmV0dXJucyAodWludDI1NikgewogICAgICAgIHJlcXVpcmUoYiAhPSAwLCAiU2FmZU1hdGhGb3JVaW50MjU2OiBtb2R1bG8gYnkgemVybyIpOwogICAgICAgIHJldHVybiBhICUgYjsKICAgIH0KCiAgICBmdW5jdGlvbiBwb3dlcih1aW50MjU2IGEsIHVpbnQyNTYgYikgaW50ZXJuYWwgcHVyZSByZXR1cm5zICh1aW50MjU2KXsKCiAgICAgICAgaWYoYSA9PSAwKSByZXR1cm4gMDsKICAgICAgICBpZihiID09IDApIHJldHVybiAxOwoKICAgICAgICB1aW50MjU2IGMgPSAxOwogICAgICAgIGZvcih1aW50MjU2IGkgPSAwOyBpIDwgYjsgaSsrKXsKICAgICAgICAgICAgYyA9IG11bChjLCBhKTsKICAgICAgICB9CiAgICB9CgogICAgZnVuY3Rpb24gbWF4KHVpbnQyNTYgYSwgdWludDI1NiBiKSBpbnRlcm5hbCBwdXJlIHJldHVybnMgKHVpbnQyNTYpIHsKICAgICAgICByZXR1cm4gYSA+PSBiID8gYSA6IGI7CiAgICB9CgogICAgZnVuY3Rpb24gbWluKHVpbnQyNTYgYSwgdWludDI1NiBiKSBpbnRlcm5hbCBwdXJlIHJldHVybnMgKHVpbnQyNTYpIHsKICAgICAgICByZXR1cm4gYSA8IGIgPyBhIDogYjsKICAgIH0KCiAgICBmdW5jdGlvbiBhdmVyYWdlKHVpbnQyNTYgYSwgdWludDI1NiBiKSBpbnRlcm5hbCBwdXJlIHJldHVybnMgKHVpbnQyNTYpIHsKICAgICAgICByZXR1cm4gKGEgLyAyKSArIChiIC8gMikgKyAoKGEgJSAyICsgYiAlIDIpIC8gMik7CiAgICB9Cn0K",
                "LibSafeMathForUint256Utils","LibSafeMathForUint256Utils");
        insertContractItem(3,1,"table","cHJhZ21hIHNvbGlkaXR5IF4wLjQuMjQ7Cgpjb250cmFjdCBUYWJsZUZhY3RvcnkgewogICAgZnVuY3Rpb24gb3BlblRhYmxlKHN0cmluZykgcHVibGljIHZpZXcgcmV0dXJucyAoVGFibGUpOyAvL29wZW4gdGFibGUKICAgIGZ1bmN0aW9uIGNyZWF0ZVRhYmxlKHN0cmluZywgc3RyaW5nLCBzdHJpbmcpIHB1YmxpYyByZXR1cm5zIChpbnQyNTYpOyAvL2NyZWF0ZSB0YWJsZQp9CgovL3NlbGVjdCBjb25kaXRpb24KY29udHJhY3QgQ29uZGl0aW9uIHsKICAgIGZ1bmN0aW9uIEVRKHN0cmluZywgaW50MjU2KSBwdWJsaWMgdmlldzsKICAgIGZ1bmN0aW9uIEVRKHN0cmluZywgc3RyaW5nKSBwdWJsaWMgdmlldzsKICAgIGZ1bmN0aW9uIEVRKHN0cmluZywgYWRkcmVzcykgcHVibGljIHZpZXc7CgogICAgZnVuY3Rpb24gTkUoc3RyaW5nLCBpbnQyNTYpIHB1YmxpYyB2aWV3OwogICAgZnVuY3Rpb24gTkUoc3RyaW5nLCBzdHJpbmcpIHB1YmxpYyB2aWV3OwoKICAgIGZ1bmN0aW9uIEdUKHN0cmluZywgaW50MjU2KSBwdWJsaWMgdmlldzsKICAgIGZ1bmN0aW9uIEdFKHN0cmluZywgaW50MjU2KSBwdWJsaWMgdmlldzsKCiAgICBmdW5jdGlvbiBMVChzdHJpbmcsIGludDI1NikgcHVibGljIHZpZXc7CiAgICBmdW5jdGlvbiBMRShzdHJpbmcsIGludDI1NikgcHVibGljIHZpZXc7CgogICAgZnVuY3Rpb24gbGltaXQoaW50MjU2KSBwdWJsaWMgdmlldzsKICAgIGZ1bmN0aW9uIGxpbWl0KGludDI1NiwgaW50MjU2KSBwdWJsaWMgdmlldzsKfQoKLy9vbmUgcmVjb3JkCmNvbnRyYWN0IEVudHJ5IHsKICAgIGZ1bmN0aW9uIGdldEludChzdHJpbmcpIHB1YmxpYyB2aWV3IHJldHVybnMgKGludDI1Nik7CiAgICBmdW5jdGlvbiBnZXRVSW50KHN0cmluZykgcHVibGljIHZpZXcgcmV0dXJucyAodWludDI1Nik7CiAgICBmdW5jdGlvbiBnZXRBZGRyZXNzKHN0cmluZykgcHVibGljIHZpZXcgcmV0dXJucyAoYWRkcmVzcyk7CiAgICBmdW5jdGlvbiBnZXRCeXRlczY0KHN0cmluZykgcHVibGljIHZpZXcgcmV0dXJucyAoYnl0ZXMxWzY0XSk7CiAgICBmdW5jdGlvbiBnZXRCeXRlczMyKHN0cmluZykgcHVibGljIHZpZXcgcmV0dXJucyAoYnl0ZXMzMik7CiAgICBmdW5jdGlvbiBnZXRTdHJpbmcoc3RyaW5nKSBwdWJsaWMgdmlldyByZXR1cm5zIChzdHJpbmcpOwoKICAgIGZ1bmN0aW9uIHNldChzdHJpbmcsIGludDI1NikgcHVibGljOwogICAgZnVuY3Rpb24gc2V0KHN0cmluZywgdWludDI1NikgcHVibGljOwogICAgZnVuY3Rpb24gc2V0KHN0cmluZywgc3RyaW5nKSBwdWJsaWM7CiAgICBmdW5jdGlvbiBzZXQoc3RyaW5nLCBhZGRyZXNzKSBwdWJsaWM7Cn0KCi8vcmVjb3JkIHNldHMKY29udHJhY3QgRW50cmllcyB7CiAgICBmdW5jdGlvbiBnZXQoaW50MjU2KSBwdWJsaWMgdmlldyByZXR1cm5zIChFbnRyeSk7CiAgICBmdW5jdGlvbiBzaXplKCkgcHVibGljIHZpZXcgcmV0dXJucyAoaW50MjU2KTsKfQoKLy9UYWJsZSBtYWluIGNvbnRyYWN0CmNvbnRyYWN0IFRhYmxlIHsKICAgIGZ1bmN0aW9uIHNlbGVjdChzdHJpbmcsIENvbmRpdGlvbikgcHVibGljIHZpZXcgcmV0dXJucyAoRW50cmllcyk7CiAgICBmdW5jdGlvbiBpbnNlcnQoc3RyaW5nLCBFbnRyeSkgcHVibGljIHJldHVybnMgKGludDI1Nik7CiAgICBmdW5jdGlvbiB1cGRhdGUoc3RyaW5nLCBFbnRyeSwgQ29uZGl0aW9uKSBwdWJsaWMgcmV0dXJucyAoaW50MjU2KTsKICAgIGZ1bmN0aW9uIHJlbW92ZShzdHJpbmcsIENvbmRpdGlvbikgcHVibGljIHJldHVybnMgKGludDI1Nik7CgogICAgZnVuY3Rpb24gbmV3RW50cnkoKSBwdWJsaWMgdmlldyByZXR1cm5zIChFbnRyeSk7CiAgICBmdW5jdGlvbiBuZXdDb25kaXRpb24oKSBwdWJsaWMgdmlldyByZXR1cm5zIChDb25kaXRpb24pOwp9Cgpjb250cmFjdCBLVlRhYmxlRmFjdG9yeSB7CiAgICBmdW5jdGlvbiBvcGVuVGFibGUoc3RyaW5nKSBwdWJsaWMgdmlldyByZXR1cm5zIChLVlRhYmxlKTsKICAgIGZ1bmN0aW9uIGNyZWF0ZVRhYmxlKHN0cmluZywgc3RyaW5nLCBzdHJpbmcpIHB1YmxpYyByZXR1cm5zIChpbnQyNTYpOwp9CgovL0tWVGFibGUgcGVyIHBlcm1pYXJ5IGtleSBoYXMgb25seSBvbmUgRW50cnkKY29udHJhY3QgS1ZUYWJsZSB7CiAgICBmdW5jdGlvbiBnZXQoc3RyaW5nKSBwdWJsaWMgdmlldyByZXR1cm5zIChib29sLCBFbnRyeSk7CiAgICBmdW5jdGlvbiBzZXQoc3RyaW5nLCBFbnRyeSkgcHVibGljIHJldHVybnMgKGludDI1Nik7CiAgICBmdW5jdGlvbiBuZXdFbnRyeSgpIHB1YmxpYyB2aWV3IHJldHVybnMgKEVudHJ5KTsKfQo=",
                "table","table");
        insertContractItem(4,2,"LibAddress","LyoKICogQ29weXJpZ2h0IDIwMTQtMjAxOSB0aGUgb3JpZ2luYWwgYXV0aG9yIG9yIGF1dGhvcnMuCiAqCiAqIExpY2Vuc2VkIHVuZGVyIHRoZSBBcGFjaGUgTGljZW5zZSwgVmVyc2lvbiAyLjAgKHRoZSAiTGljZW5zZSIpOwogKiB5b3UgbWF5IG5vdCB1c2UgdGhpcyBmaWxlIGV4Y2VwdCBpbiBjb21wbGlhbmNlIHdpdGggdGhlIExpY2Vuc2UuCiAqIFlvdSBtYXkgb2J0YWluIGEgY29weSBvZiB0aGUgTGljZW5zZSBhdAogKgogKiAgICAgIGh0dHA6Ly93d3cuYXBhY2hlLm9yZy9saWNlbnNlcy9MSUNFTlNFLTIuMAogKgogKiBVbmxlc3MgcmVxdWlyZWQgYnkgYXBwbGljYWJsZSBsYXcgb3IgYWdyZWVkIHRvIGluIHdyaXRpbmcsIHNvZnR3YXJlCiAqIGRpc3RyaWJ1dGVkIHVuZGVyIHRoZSBMaWNlbnNlIGlzIGRpc3RyaWJ1dGVkIG9uIGFuICJBUyBJUyIgQkFTSVMsCiAqIFdJVEhPVVQgV0FSUkFOVElFUyBPUiBDT05ESVRJT05TIE9GIEFOWSBLSU5ELCBlaXRoZXIgZXhwcmVzcyBvciBpbXBsaWVkLgogKiBTZWUgdGhlIExpY2Vuc2UgZm9yIHRoZSBzcGVjaWZpYyBsYW5ndWFnZSBnb3Zlcm5pbmcgcGVybWlzc2lvbnMgYW5kCiAqIGxpbWl0YXRpb25zIHVuZGVyIHRoZSBMaWNlbnNlLgogKiAqLwoKcHJhZ21hIHNvbGlkaXR5IF4wLjQuMjU7CgpsaWJyYXJ5IExpYkFkZHJlc3N7CgogICAgZnVuY3Rpb24gaXNDb250cmFjdChhZGRyZXNzIGFjY291bnQpIGludGVybmFsIHZpZXcgcmV0dXJucyhib29sKSB7CiAgICAgICAgdWludDI1NiBzaXplOwogICAgICAgIGFzc2VtYmx5IHsgc2l6ZSA6PSBleHRjb2Rlc2l6ZShhZGRyKSB9ICAKICAgICAgICByZXR1cm4gc2l6ZSA",
                "LibAddress","LibAddress");
        insertContractItem(5,3,"LibAddress","LyoKICogQ29weXJpZ2h0IDIwMTQtMjAxOSB0aGUgb3JpZ2luYWwgYXV0aG9yIG9yIGF1dGhvcnMuCiAqCiAqIExpY2Vuc2VkIHVuZGVyIHRoZSBBcGFjaGUgTGljZW5zZSwgVmVyc2lvbiAyLjAgKHRoZSAiTGljZW5zZSIpOwogKiB5b3UgbWF5IG5vdCB1c2UgdGhpcyBmaWxlIGV4Y2VwdCBpbiBjb21wbGlhbmNlIHdpdGggdGhlIExpY2Vuc2UuCiAqIFlvdSBtYXkgb2J0YWluIGEgY29weSBvZiB0aGUgTGljZW5zZSBhdAogKgogKiAgICAgIGh0dHA6Ly93d3cuYXBhY2hlLm9yZy9saWNlbnNlcy9MSUNFTlNFLTIuMAogKgogKiBVbmxlc3MgcmVxdWlyZWQgYnkgYXBwbGljYWJsZSBsYXcgb3IgYWdyZWVkIHRvIGluIHdyaXRpbmcsIHNvZnR3YXJlCiAqIGRpc3RyaWJ1dGVkIHVuZGVyIHRoZSBMaWNlbnNlIGlzIGRpc3RyaWJ1dGVkIG9uIGFuICJBUyBJUyIgQkFTSVMsCiAqIFdJVEhPVVQgV0FSUkFOVElFUyBPUiBDT05ESVRJT05TIE9GIEFOWSBLSU5ELCBlaXRoZXIgZXhwcmVzcyBvciBpbXBsaWVkLgogKiBTZWUgdGhlIExpY2Vuc2UgZm9yIHRoZSBzcGVjaWZpYyBsYW5ndWFnZSBnb3Zlcm5pbmcgcGVybWlzc2lvbnMgYW5kCiAqIGxpbWl0YXRpb25zIHVuZGVyIHRoZSBMaWNlbnNlLgogKiAqLwoKcHJhZ21hIHNvbGlkaXR5IF4wLjQuMjU7CgpsaWJyYXJ5IExpYkFkZHJlc3N7CgogICAgZnVuY3Rpb24gaXNDb250cmFjdChhZGRyZXNzIGFjY291bnQpIGludGVybmFsIHZpZXcgcmV0dXJucyhib29sKSB7CiAgICAgICAgdWludDI1NiBzaXplOwogICAgICAgIGFzc2VtYmx5IHsgc2l6ZSA6PSBleHRjb2Rlc2l6ZShhZGRyKSB9ICAKICAgICAgICByZXR1cm4gc2l6ZSA",
                "LibAddress","LibAddress");
        insertContractItem(6,3,"LibAddress","LyoKICogQ29weXJpZ2h0IDIwMTQtMjAxOSB0aGUgb3JpZ2luYWwgYXV0aG9yIG9yIGF1dGhvcnMuCiAqCiAqIExpY2Vuc2VkIHVuZGVyIHRoZSBBcGFjaGUgTGljZW5zZSwgVmVyc2lvbiAyLjAgKHRoZSAiTGljZW5zZSIpOwogKiB5b3UgbWF5IG5vdCB1c2UgdGhpcyBmaWxlIGV4Y2VwdCBpbiBjb21wbGlhbmNlIHdpdGggdGhlIExpY2Vuc2UuCiAqIFlvdSBtYXkgb2J0YWluIGEgY29weSBvZiB0aGUgTGljZW5zZSBhdAogKgogKiAgICAgIGh0dHA6Ly93d3cuYXBhY2hlLm9yZy9saWNlbnNlcy9MSUNFTlNFLTIuMAogKgogKiBVbmxlc3MgcmVxdWlyZWQgYnkgYXBwbGljYWJsZSBsYXcgb3IgYWdyZWVkIHRvIGluIHdyaXRpbmcsIHNvZnR3YXJlCiAqIGRpc3RyaWJ1dGVkIHVuZGVyIHRoZSBMaWNlbnNlIGlzIGRpc3RyaWJ1dGVkIG9uIGFuICJBUyBJUyIgQkFTSVMsCiAqIFdJVEhPVVQgV0FSUkFOVElFUyBPUiBDT05ESVRJT05TIE9GIEFOWSBLSU5ELCBlaXRoZXIgZXhwcmVzcyBvciBpbXBsaWVkLgogKiBTZWUgdGhlIExpY2Vuc2UgZm9yIHRoZSBzcGVjaWZpYyBsYW5ndWFnZSBnb3Zlcm5pbmcgcGVybWlzc2lvbnMgYW5kCiAqIGxpbWl0YXRpb25zIHVuZGVyIHRoZSBMaWNlbnNlLgogKiAqLwoKcHJhZ21hIHNvbGlkaXR5IF4wLjQuMjU7CgpsaWJyYXJ5IExpYkFkZHJlc3N7CgogICAgZnVuY3Rpb24gaXNDb250cmFjdChhZGRyZXNzIGFjY291bnQpIGludGVybmFsIHZpZXcgcmV0dXJucyhib29sKSB7CiAgICAgICAgdWludDI1NiBzaXplOwogICAgICAgIGFzc2VtYmx5IHsgc2l6ZSA6PSBleHRjb2Rlc2l6ZShhZGRyKSB9ICAKICAgICAgICByZXR1cm4gc2l6ZSA",
                "LibAddress","LibAddress");
    }


    public void insertContractItem(long contractId, long contractFolderId, String contractName, String contractSrc,
                                String contractDesc, String contractDesc_en)
    {
        ContractItem contractItem = new ContractItem();
        contractItem.setContractId(contractId);
        contractItem.setContractFolderId(contractFolderId);
        contractItem.setContractName(contractName);
        contractItem.setContractSrc(contractSrc);
        contractItem.setContractDesc(contractDesc);
        contractItem.setContractDesc_en(contractDesc_en);
        contractItem.setCreateTime(LocalDateTime.now());
        contractItem.setModifyTime(contractItem.getCreateTime());
        contractItemRepository.save(contractItem);
    }

    public void initPresetData()
    {
        List<ContractItem> contracts = contractItemRepository.findAll();
        if(contracts.size() <= 0)
        {
            initContractItem();
        }

        List<ContractFolderItem> contractFolderItemList = contractFolderRepository.findAll();
        if(contractFolderItemList.size() <= 0)
        {
            initContractFolderItem();
        }

        List<StoreItem> storeItemList = contractStoreRepository.findAll();
        if(storeItemList.size() <= 0)
        {
            initStoreItem();
        }
    }

}
