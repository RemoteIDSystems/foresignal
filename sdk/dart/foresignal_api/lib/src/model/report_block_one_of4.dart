//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/principal_summary.dart';
import 'package:foresignal_api/src/model/report_block_one_of4_all_of_items_inner.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'report_block_one_of4.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ReportBlockOneOf4 {
  /// Returns a new [ReportBlockOneOf4] instance.
  ReportBlockOneOf4({

     this.id,

    required  this.type,

     this.title,

     this.textOrigin,

     this.updatedAt,

     this.updatedBy,

     this.sourceRef,

    required  this.items,
  });

      /// Server-assigned; comment on a block with `report:{id}#{blockId}`
  @JsonKey(
    
    name: r'id',
    required: false,
    includeIfNull: false,
  )


  final String? id;



  @JsonKey(
    
    name: r'type',
    required: true,
    includeIfNull: false,
  )


  final ReportBlockOneOf4TypeEnum type;



  @JsonKey(
    
    name: r'title',
    required: false,
    includeIfNull: false,
  )


  final String? title;



      /// `llm` blocks are labelled narrative from the optional 028 tool loop
  @JsonKey(
    
    name: r'textOrigin',
    required: false,
    includeIfNull: false,
  )


  final ReportBlockOneOf4TextOriginEnum? textOrigin;



  @JsonKey(
    
    name: r'updatedAt',
    required: false,
    includeIfNull: false,
  )


  final DateTime? updatedAt;



  @JsonKey(
    
    name: r'updatedBy',
    required: false,
    includeIfNull: false,
  )


  final PrincipalSummary? updatedBy;



      /// Canonical object reference `{type}:{id}` (docs/Client-Contract.md §2). Record refs are `record:{sourceId}/{externalId}[@{seq}]` with the external id percent-encoded. Remote refs (132) are `remote:{instanceId}/{type}:{id}`: read-only material received from another instance.
  @JsonKey(
    
    name: r'sourceRef',
    required: false,
    includeIfNull: false,
  )


  final String? sourceRef;



  @JsonKey(
    
    name: r'items',
    required: true,
    includeIfNull: false,
  )


  final List<ReportBlockOneOf4AllOfItemsInner> items;





    @override
    bool operator ==(Object other) => identical(this, other) || other is ReportBlockOneOf4 &&
      other.id == id &&
      other.type == type &&
      other.title == title &&
      other.textOrigin == textOrigin &&
      other.updatedAt == updatedAt &&
      other.updatedBy == updatedBy &&
      other.sourceRef == sourceRef &&
      other.items == items;

    @override
    int get hashCode =>
        id.hashCode +
        type.hashCode +
        (title == null ? 0 : title.hashCode) +
        textOrigin.hashCode +
        updatedAt.hashCode +
        (updatedBy == null ? 0 : updatedBy.hashCode) +
        (sourceRef == null ? 0 : sourceRef.hashCode) +
        items.hashCode;

  factory ReportBlockOneOf4.fromJson(Map<String, dynamic> json) => _$ReportBlockOneOf4FromJson(json);

  Map<String, dynamic> toJson() => _$ReportBlockOneOf4ToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum ReportBlockOneOf4TypeEnum {
@JsonValue(r'timeline')
timeline(r'timeline');

const ReportBlockOneOf4TypeEnum(this.value);

final String value;

@override
String toString() => value;
}


/// `llm` blocks are labelled narrative from the optional 028 tool loop
enum ReportBlockOneOf4TextOriginEnum {
@JsonValue(r'author')
author(r'author'),
@JsonValue(r'deterministic')
deterministic(r'deterministic'),
@JsonValue(r'llm')
llm(r'llm');

const ReportBlockOneOf4TextOriginEnum(this.value);

final String value;

@override
String toString() => value;
}


