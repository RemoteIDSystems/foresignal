//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/attribution.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'exposure_population.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ExposurePopulation {
  /// Returns a new [ExposurePopulation] instance.
  ExposurePopulation({

    required  this.status,

     this.attribution,

     this.estimate,

     this.epoch,

     this.method,

     this.note,
  });

  @JsonKey(
    
    name: r'status',
    required: true,
    includeIfNull: false,
  )


  final ExposurePopulationStatusEnum status;



  @JsonKey(
    
    name: r'attribution',
    required: false,
    includeIfNull: false,
  )


  final List<Attribution>? attribution;



  @JsonKey(
    
    name: r'estimate',
    required: false,
    includeIfNull: false,
  )


  final num? estimate;



  @JsonKey(
    
    name: r'epoch',
    required: false,
    includeIfNull: false,
  )


  final int? epoch;



  @JsonKey(
    
    name: r'method',
    required: false,
    includeIfNull: false,
  )


  final String? method;



  @JsonKey(
    
    name: r'note',
    required: false,
    includeIfNull: false,
  )


  final String? note;





    @override
    bool operator ==(Object other) => identical(this, other) || other is ExposurePopulation &&
      other.status == status &&
      other.attribution == attribution &&
      other.estimate == estimate &&
      other.epoch == epoch &&
      other.method == method &&
      other.note == note;

    @override
    int get hashCode =>
        status.hashCode +
        attribution.hashCode +
        (estimate == null ? 0 : estimate.hashCode) +
        (epoch == null ? 0 : epoch.hashCode) +
        (method == null ? 0 : method.hashCode) +
        (note == null ? 0 : note.hashCode);

  factory ExposurePopulation.fromJson(Map<String, dynamic> json) => _$ExposurePopulationFromJson(json);

  Map<String, dynamic> toJson() => _$ExposurePopulationToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum ExposurePopulationStatusEnum {
@JsonValue(r'ok')
ok(r'ok'),
@JsonValue(r'unavailable')
unavailable(r'unavailable'),
@JsonValue(r'not-installed')
notInstalled(r'not-installed');

const ExposurePopulationStatusEnum(this.value);

final String value;

@override
String toString() => value;
}


