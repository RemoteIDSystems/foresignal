//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/coverage_stats.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'dossier_coverage.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class DossierCoverage {
  /// Returns a new [DossierCoverage] instance.
  DossierCoverage({

    required  this.status,

     this.stats,
  });

  @JsonKey(
    
    name: r'status',
    required: true,
    includeIfNull: false,
  )


  final DossierCoverageStatusEnum status;



  @JsonKey(
    
    name: r'stats',
    required: false,
    includeIfNull: false,
  )


  final CoverageStats? stats;





    @override
    bool operator ==(Object other) => identical(this, other) || other is DossierCoverage &&
      other.status == status &&
      other.stats == stats;

    @override
    int get hashCode =>
        status.hashCode +
        (stats == null ? 0 : stats.hashCode);

  factory DossierCoverage.fromJson(Map<String, dynamic> json) => _$DossierCoverageFromJson(json);

  Map<String, dynamic> toJson() => _$DossierCoverageToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum DossierCoverageStatusEnum {
@JsonValue(r'ok')
ok(r'ok'),
@JsonValue(r'partial')
partial(r'partial'),
@JsonValue(r'unavailable')
unavailable(r'unavailable');

const DossierCoverageStatusEnum(this.value);

final String value;

@override
String toString() => value;
}


