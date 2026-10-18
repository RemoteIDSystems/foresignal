//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'lightning_cluster_payload.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class LightningClusterPayload {
  /// Returns a new [LightningClusterPayload] instance.
  LightningClusterPayload({

    required  this.flashes5m,

     this.radiusKm,

     this.maxEnergyFj,

    required  this.label,

    required  this.automated,

    required  this.notAWarning,
  });

      /// Bucket lower bound
  @JsonKey(
    
    name: r'flashes5m',
    required: true,
    includeIfNull: false,
  )


  final LightningClusterPayloadFlashes5mEnum flashes5m;



  @JsonKey(
    
    name: r'radiusKm',
    required: false,
    includeIfNull: false,
  )


  final num? radiusKm;



      /// Bucketed
  @JsonKey(
    
    name: r'maxEnergyFj',
    required: false,
    includeIfNull: false,
  )


  final num? maxEnergyFj;



  @JsonKey(
    
    name: r'label',
    required: true,
    includeIfNull: false,
  )


  final String label;



  @JsonKey(
    
    name: r'automated',
    required: true,
    includeIfNull: false,
  )


  final bool automated;



  @JsonKey(
    
    name: r'notAWarning',
    required: true,
    includeIfNull: false,
  )


  final bool notAWarning;





    @override
    bool operator ==(Object other) => identical(this, other) || other is LightningClusterPayload &&
      other.flashes5m == flashes5m &&
      other.radiusKm == radiusKm &&
      other.maxEnergyFj == maxEnergyFj &&
      other.label == label &&
      other.automated == automated &&
      other.notAWarning == notAWarning;

    @override
    int get hashCode =>
        flashes5m.hashCode +
        radiusKm.hashCode +
        maxEnergyFj.hashCode +
        label.hashCode +
        automated.hashCode +
        notAWarning.hashCode;

  factory LightningClusterPayload.fromJson(Map<String, dynamic> json) => _$LightningClusterPayloadFromJson(json);

  Map<String, dynamic> toJson() => _$LightningClusterPayloadToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

/// Bucket lower bound
enum LightningClusterPayloadFlashes5mEnum {
@JsonValue(3)
number3('3'),
@JsonValue(5)
number5('5'),
@JsonValue(10)
number10('10'),
@JsonValue(20)
number20('20'),
@JsonValue(50)
number50('50'),
@JsonValue(100)
number100('100'),
@JsonValue(200)
number200('200');

const LightningClusterPayloadFlashes5mEnum(this.value);

final String value;

@override
String toString() => value;
}


