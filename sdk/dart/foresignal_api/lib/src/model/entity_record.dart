//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/entity_link.dart';
import 'package:foresignal_api/src/model/record.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'entity_record.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class EntityRecord {
  /// Returns a new [EntityRecord] instance.
  EntityRecord({

    required  this.record,

    required  this.link,
  });

  @JsonKey(
    
    name: r'record',
    required: true,
    includeIfNull: false,
  )


  final Record record;



  @JsonKey(
    
    name: r'link',
    required: true,
    includeIfNull: false,
  )


  final EntityLink link;





    @override
    bool operator ==(Object other) => identical(this, other) || other is EntityRecord &&
      other.record == record &&
      other.link == link;

    @override
    int get hashCode =>
        record.hashCode +
        link.hashCode;

  factory EntityRecord.fromJson(Map<String, dynamic> json) => _$EntityRecordFromJson(json);

  Map<String, dynamic> toJson() => _$EntityRecordToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

