//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/impact_rate_interval.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'precision_components_inner.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class PrecisionComponentsInner {
  /// Returns a new [PrecisionComponentsInner] instance.
  PrecisionComponentsInner({

    required  this.component,

    required  this.key,

    required  this.confirmed,

    required  this.rejected,

     this.duplicate,

     this.needsReview,

    required  this.labelled,

     this.precision,

     this.interval,
  });

  @JsonKey(
    
    name: r'component',
    required: true,
    includeIfNull: false,
  )


  final PrecisionComponentsInnerComponentEnum component;



      /// Detector, recipe or linker id and version
  @JsonKey(
    
    name: r'key',
    required: true,
    includeIfNull: false,
  )


  final String key;



  @JsonKey(
    
    name: r'confirmed',
    required: true,
    includeIfNull: false,
  )


  final int confirmed;



  @JsonKey(
    
    name: r'rejected',
    required: true,
    includeIfNull: false,
  )


  final int rejected;



  @JsonKey(
    
    name: r'duplicate',
    required: false,
    includeIfNull: false,
  )


  final int? duplicate;



      /// Counted as unlabelled
  @JsonKey(
    
    name: r'needsReview',
    required: false,
    includeIfNull: false,
  )


  final int? needsReview;



  @JsonKey(
    
    name: r'labelled',
    required: true,
    includeIfNull: false,
  )


  final int labelled;



  @JsonKey(
    
    name: r'precision',
    required: false,
    includeIfNull: false,
  )


  final num? precision;



  @JsonKey(
    
    name: r'interval',
    required: false,
    includeIfNull: false,
  )


  final ImpactRateInterval? interval;





    @override
    bool operator ==(Object other) => identical(this, other) || other is PrecisionComponentsInner &&
      other.component == component &&
      other.key == key &&
      other.confirmed == confirmed &&
      other.rejected == rejected &&
      other.duplicate == duplicate &&
      other.needsReview == needsReview &&
      other.labelled == labelled &&
      other.precision == precision &&
      other.interval == interval;

    @override
    int get hashCode =>
        component.hashCode +
        key.hashCode +
        confirmed.hashCode +
        rejected.hashCode +
        duplicate.hashCode +
        needsReview.hashCode +
        labelled.hashCode +
        (precision == null ? 0 : precision.hashCode) +
        (interval == null ? 0 : interval.hashCode);

  factory PrecisionComponentsInner.fromJson(Map<String, dynamic> json) => _$PrecisionComponentsInnerFromJson(json);

  Map<String, dynamic> toJson() => _$PrecisionComponentsInnerToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum PrecisionComponentsInnerComponentEnum {
@JsonValue(r'finding')
finding(r'finding'),
@JsonValue(r'intel-event')
intelEvent(r'intel-event'),
@JsonValue(r'situation')
situation(r'situation'),
@JsonValue(r'precursor-active')
precursorActive(r'precursor-active');

const PrecisionComponentsInnerComponentEnum(this.value);

final String value;

@override
String toString() => value;
}


