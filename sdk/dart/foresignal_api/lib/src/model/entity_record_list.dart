//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/entity_record.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'entity_record_list.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class EntityRecordList {
  /// Returns a new [EntityRecordList] instance.
  EntityRecordList({

    required  this.entityId,

     this.at,

    required  this.items,
  });

      /// ULID identifier
  @JsonKey(
    
    name: r'entityId',
    required: true,
    includeIfNull: false,
  )


  final String entityId;



      /// Validity instant the links were selected at
  @JsonKey(
    
    name: r'at',
    required: false,
    includeIfNull: false,
  )


  final DateTime? at;



  @JsonKey(
    
    name: r'items',
    required: true,
    includeIfNull: false,
  )


  final List<EntityRecord> items;





    @override
    bool operator ==(Object other) => identical(this, other) || other is EntityRecordList &&
      other.entityId == entityId &&
      other.at == at &&
      other.items == items;

    @override
    int get hashCode =>
        entityId.hashCode +
        (at == null ? 0 : at.hashCode) +
        items.hashCode;

  factory EntityRecordList.fromJson(Map<String, dynamic> json) => _$EntityRecordListFromJson(json);

  Map<String, dynamic> toJson() => _$EntityRecordListToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

