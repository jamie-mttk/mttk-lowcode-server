db.page.aggregate([
    //1.0 Owner check
    //1.1 If account is in owner list, create full opeartion list,otherwise create empty operation list
    {
        $addFields: {
            _operationsOwners: {
                 $cond: { if: { $in: ["65d80960f164234e3b72b4e9", { $ifNull: [ "$_owners",[] ] }] }, then: ["access", "edit", "del", "auth"], else: [] }
            }
        }
    },
    //2.0 Owner group check
    //2.1 Find all the matching owner group into _owner_groups
    {
        $lookup:
            {
                from: "account",
                let: { my_owners: "$_owners" },
                pipeline: [
                    {
                        $match:
                            {  
                                $expr: { $in: [{ $toString: "$_id" }, { $ifNull: [ "$$my_owners",[] ] }] }
                            }
                    },
                    { $project: { _id: 0, groups: 1 } },

                    { $unwind: "$groups" },
                    { $group: { _id: "$groups" } },
                    {
                        $match:
                            { "_id": { "$in": ["65d8089ef164234e3b72b4e6q", "65d8089ef164234e3b72b4e5", "AABBCC"] } }
                    },

                ],

                as: "_owner_groups"
            }

    },
    //2.2 calculate owner group operations into _operationsOwnerGroups
    //2.2.1 Try to find the owner group operations from _authorities with type ownerGroup
    {
        $addFields: {

            "_operationsOwnerGroups": {
                "$filter": {
                    "input":  { $ifNull: [ "$_authorities",[] ] },
                    "as": "a",
                    "cond": { $eq: ["$$a.type", "ownerGroup"] }

                }
            }

        }
    },
    //2.2.2 if  _operationsOwnerGroups is NOT empty get first one,otherwise set to  {operations:[]}
    {
        $addFields: {
            _operationsOwnerGroups: {
                $cond: { if: { $gt: [{ $size: {$ifNull:["$_operationsOwnerGroups",[]]} }, 0] }, then: { $arrayElemAt: ["$_operationsOwnerGroups", 0] }, else: { operations: [] } }
            }
        }
    },
    //2.2.3 Get operations under _operationsOwnerGroups as _operationsOwnerGroups
    {
        $addFields: {
            _operationsOwnerGroups: "$_operationsOwnerGroups.operations"
        }
    },
    // 2.2.4 if _owner_groups is NOT empty(owner group matched),add field _operationsOwnerGroups with all the operations in authorities of type ownerGroup
    {
        $addFields: {
            _operationsOwnerGroups: {
                $cond: { if: { $gt: [{ $size:  {$ifNull:["$_owner_groups",[]]}   }, 0] }, then: "$_operationsOwnerGroups", else: [] }
            }
        }
    },
    //2.2.5 Remove _owner_groups
    { $unset: ["_owner_groups"] },

    //3.0 Handle _authorities with type group and user
    //3.1 Only keep the authorities which is matched(owner group is evaled so far)  
    {
        $addFields: {
            "_operationsOther": {
                "$filter": {
                    "input":  { $ifNull: [ "$_authorities",[] ] },
                    "as": "a",
                    "cond": {
                        $switch: {
                            branches: [
                                {
                                    case: { $eq: ["$$a.type", "group"] }, then: {

                                        "$in": [
                                            "$$a.id",
                                            ["65d808c7f164234e3b72b4e6", "AABBCC"]

                                        ]
                                    }
                                },
                                {
                                    case: { $eq: ["$$a.type", "user"] }, then: {

                                        "$eq": [
                                            "$$a.id",
                                            "65d80960f164234e3b72b4e9"
                                        ]

                                    }
                                },
                            ],
                            default: false
                        }
                    }
                }
            }

        }
    },
    //3.2 Copy operations under _operationsOther to root
    {
        $addFields: {
            "_operationsOther": { $concatArrays: ["$_operationsOther.operations"] }
        }
    },
    //3.3 merge all the operations together
    {
        $addFields: {
            "_operationsOther": {
                "$reduce": {
                    "input": "$_operationsOther",
                    "initialValue": [],
                    "in": { "$setUnion": ["$$this", "$$value"] }
                }
            }
        }
    },
    //x.0 handle all_read
    {
        $addFields: {
            "_operationsAllRead": ['access']
        }
    },
    //4.0 Final
    //4.1 combine all the operations
    {
        $addFields: {
            "_operationsAll": { "$setUnion": ["$_operationsOwners", "$_operationsOwnerGroups", "$_operationsOther","$_operationsAllRead"] }
        }
    },
    //4.2 Combine the operations set in account role. On the other word, remove the operations in _operationsAll which is not in the account role resource list
    {
        $addFields: {
            "_operationsAll": {
                "$filter": {
                    "input": "$_operationsAll",
                    "as": "o",
                    "cond": { $in: ["$$o", ["access", "del", "wowo"]] }

                }
            }
        }
    },
    //4.3
    {
        $addFields: {
            "_operationCount": { $size: {$ifNull:["$_operationsAll",[]]} }
        }
    },
    //4.4 only keep the matched records
    { $match: { "_operationCount": { $gt: 0 } } },
    //4.5 Remove _operationCount
    { $unset: ["_operationsOwners", "_operationsOwnerGroups", "_operationsOther", "_operationCount"] },


])