//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'chronicle_state_layers_inner_rollups_inner.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ChronicleStateLayersInnerRollupsInner {
  /// Returns a new [ChronicleStateLayersInnerRollupsInner] instance.
  ChronicleStateLayersInnerRollupsInner({

    required  this.region,

    required  this.hour,

    required  this.count,
  });

  @JsonKey(
    
    name: r'region',
    required: true,
    includeIfNull: false,
  )


  final String region;



  @JsonKey(
    
    name: r'hour',
    required: true,
    includeIfNull: false,
  )


  final DateTime hour;



  @JsonKey(
    
    name: r'count',
    required: true,
    includeIfNull: false,
  )


  final int count;





    @override
    bool operator ==(Object other) => identical(this, other) || other is ChronicleStateLayersInnerRollupsInner &&
      other.region == region &&
      other.hour == hour &&
      other.count == count;

    @override
    int get hashCode =>
        region.hashCode +
        hour.hashCode +
        count.hashCode;

  factory ChronicleStateLayersInnerRollupsInner.fromJson(Map<String, dynamic> json) => _$ChronicleStateLayersInnerRollupsInnerFromJson(json);

  Map<String, dynamic> toJson() => _$ChronicleStateLayersInnerRollupsInnerToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

