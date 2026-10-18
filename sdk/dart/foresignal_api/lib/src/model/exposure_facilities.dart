//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/attribution.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'exposure_facilities.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ExposureFacilities {
  /// Returns a new [ExposureFacilities] instance.
  ExposureFacilities({

    required  this.status,

     this.attribution,

     this.counts,

     this.total,
  });

  @JsonKey(
    
    name: r'status',
    required: true,
    includeIfNull: false,
  )


  final ExposureFacilitiesStatusEnum status;



  @JsonKey(
    
    name: r'attribution',
    required: false,
    includeIfNull: false,
  )


  final List<Attribution>? attribution;



      /// power-plant, airport, port, cable-landing, data-centre, nuclear-facility, spaceport
  @JsonKey(
    
    name: r'counts',
    required: false,
    includeIfNull: false,
  )


  final Map<String, int>? counts;



  @JsonKey(
    
    name: r'total',
    required: false,
    includeIfNull: false,
  )


  final int? total;





    @override
    bool operator ==(Object other) => identical(this, other) || other is ExposureFacilities &&
      other.status == status &&
      other.attribution == attribution &&
      other.counts == counts &&
      other.total == total;

    @override
    int get hashCode =>
        status.hashCode +
        attribution.hashCode +
        counts.hashCode +
        total.hashCode;

  factory ExposureFacilities.fromJson(Map<String, dynamic> json) => _$ExposureFacilitiesFromJson(json);

  Map<String, dynamic> toJson() => _$ExposureFacilitiesToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum ExposureFacilitiesStatusEnum {
@JsonValue(r'ok')
ok(r'ok'),
@JsonValue(r'unavailable')
unavailable(r'unavailable'),
@JsonValue(r'not-installed')
notInstalled(r'not-installed');

const ExposureFacilitiesStatusEnum(this.value);

final String value;

@override
String toString() => value;
}


