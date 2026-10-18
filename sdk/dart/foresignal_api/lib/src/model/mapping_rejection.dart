//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'mapping_rejection.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class MappingRejection {
  /// Returns a new [MappingRejection] instance.
  MappingRejection({

    required  this.index,

    required  this.reason,

     this.externalId,
  });

  @JsonKey(
    
    name: r'index',
    required: true,
    includeIfNull: false,
  )


  final int index;



  @JsonKey(
    
    name: r'reason',
    required: true,
    includeIfNull: false,
  )


  final String reason;



  @JsonKey(
    
    name: r'externalId',
    required: false,
    includeIfNull: false,
  )


  final String? externalId;





    @override
    bool operator ==(Object other) => identical(this, other) || other is MappingRejection &&
      other.index == index &&
      other.reason == reason &&
      other.externalId == externalId;

    @override
    int get hashCode =>
        index.hashCode +
        reason.hashCode +
        (externalId == null ? 0 : externalId.hashCode);

  factory MappingRejection.fromJson(Map<String, dynamic> json) => _$MappingRejectionFromJson(json);

  Map<String, dynamic> toJson() => _$MappingRejectionToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

