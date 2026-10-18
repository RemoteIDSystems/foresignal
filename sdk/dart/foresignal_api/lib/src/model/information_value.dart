//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/information_value_indicators_inner.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'information_value.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class InformationValue {
  /// Returns a new [InformationValue] instance.
  InformationValue({

    required  this.setId,

    required  this.computedAt,

    required  this.entropy,

     this.version,

    required  this.indicators,
  });

      /// ULID identifier
  @JsonKey(
    
    name: r'setId',
    required: true,
    includeIfNull: false,
  )


  final String setId;



  @JsonKey(
    
    name: r'computedAt',
    required: true,
    includeIfNull: false,
  )


  final DateTime computedAt;



      /// H(P) of the set's current distribution (nats)
  @JsonKey(
    
    name: r'entropy',
    required: true,
    includeIfNull: false,
  )


  final num entropy;



  @JsonKey(
    
    name: r'version',
    required: false,
    includeIfNull: false,
  )


  final String? version;



  @JsonKey(
    
    name: r'indicators',
    required: true,
    includeIfNull: false,
  )


  final List<InformationValueIndicatorsInner> indicators;





    @override
    bool operator ==(Object other) => identical(this, other) || other is InformationValue &&
      other.setId == setId &&
      other.computedAt == computedAt &&
      other.entropy == entropy &&
      other.version == version &&
      other.indicators == indicators;

    @override
    int get hashCode =>
        setId.hashCode +
        computedAt.hashCode +
        entropy.hashCode +
        version.hashCode +
        indicators.hashCode;

  factory InformationValue.fromJson(Map<String, dynamic> json) => _$InformationValueFromJson(json);

  Map<String, dynamic> toJson() => _$InformationValueToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

