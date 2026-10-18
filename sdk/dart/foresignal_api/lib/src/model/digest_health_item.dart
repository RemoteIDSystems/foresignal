//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'digest_health_item.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class DigestHealthItem {
  /// Returns a new [DigestHealthItem] instance.
  DigestHealthItem({

    required  this.sourceId,

    required  this.status,

     this.since,

     this.lastError,
  });

  @JsonKey(
    
    name: r'sourceId',
    required: true,
    includeIfNull: false,
  )


  final String sourceId;



  @JsonKey(
    
    name: r'status',
    required: true,
    includeIfNull: false,
  )


  final DigestHealthItemStatusEnum status;



      /// Since when the source has been in this state; null while health transitions are not stored
  @JsonKey(
    
    name: r'since',
    required: false,
    includeIfNull: false,
  )


  final DateTime? since;



  @JsonKey(
    
    name: r'lastError',
    required: false,
    includeIfNull: false,
  )


  final String? lastError;





    @override
    bool operator ==(Object other) => identical(this, other) || other is DigestHealthItem &&
      other.sourceId == sourceId &&
      other.status == status &&
      other.since == since &&
      other.lastError == lastError;

    @override
    int get hashCode =>
        sourceId.hashCode +
        status.hashCode +
        (since == null ? 0 : since.hashCode) +
        (lastError == null ? 0 : lastError.hashCode);

  factory DigestHealthItem.fromJson(Map<String, dynamic> json) => _$DigestHealthItemFromJson(json);

  Map<String, dynamic> toJson() => _$DigestHealthItemToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum DigestHealthItemStatusEnum {
@JsonValue(r'degraded')
degraded(r'degraded'),
@JsonValue(r'failing')
failing(r'failing');

const DigestHealthItemStatusEnum(this.value);

final String value;

@override
String toString() => value;
}


