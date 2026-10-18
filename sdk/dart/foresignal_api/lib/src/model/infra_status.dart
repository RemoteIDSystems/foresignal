//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/infra_build.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'infra_status.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class InfraStatus {
  /// Returns a new [InfraStatus] instance.
  InfraStatus({

    required  this.built,

    required  this.building,

     this.lastBuild,

     this.nodesByType,

     this.osmExtractDate,
  });

  @JsonKey(
    
    name: r'built',
    required: true,
    includeIfNull: false,
  )


  final bool built;



  @JsonKey(
    
    name: r'building',
    required: true,
    includeIfNull: false,
  )


  final bool building;



  @JsonKey(
    
    name: r'lastBuild',
    required: false,
    includeIfNull: false,
  )


  final InfraBuild? lastBuild;



  @JsonKey(
    
    name: r'nodesByType',
    required: false,
    includeIfNull: false,
  )


  final Map<String, int>? nodesByType;



  @JsonKey(
    
    name: r'osmExtractDate',
    required: false,
    includeIfNull: false,
  )


  final DateTime? osmExtractDate;





    @override
    bool operator ==(Object other) => identical(this, other) || other is InfraStatus &&
      other.built == built &&
      other.building == building &&
      other.lastBuild == lastBuild &&
      other.nodesByType == nodesByType &&
      other.osmExtractDate == osmExtractDate;

    @override
    int get hashCode =>
        built.hashCode +
        building.hashCode +
        (lastBuild == null ? 0 : lastBuild.hashCode) +
        nodesByType.hashCode +
        (osmExtractDate == null ? 0 : osmExtractDate.hashCode);

  factory InfraStatus.fromJson(Map<String, dynamic> json) => _$InfraStatusFromJson(json);

  Map<String, dynamic> toJson() => _$InfraStatusToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

