//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'alert_context_near.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class AlertContextNear {
  /// Returns a new [AlertContextNear] instance.
  AlertContextNear({

     this.placeIds,

     this.placeVersions,
  });

  @JsonKey(
    
    name: r'placeIds',
    required: false,
    includeIfNull: false,
  )


  final List<String>? placeIds;



  @JsonKey(
    
    name: r'placeVersions',
    required: false,
    includeIfNull: false,
  )


  final List<int>? placeVersions;





    @override
    bool operator ==(Object other) => identical(this, other) || other is AlertContextNear &&
      other.placeIds == placeIds &&
      other.placeVersions == placeVersions;

    @override
    int get hashCode =>
        placeIds.hashCode +
        placeVersions.hashCode;

  factory AlertContextNear.fromJson(Map<String, dynamic> json) => _$AlertContextNearFromJson(json);

  Map<String, dynamic> toJson() => _$AlertContextNearToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

