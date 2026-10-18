//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/attribution.dart';
import 'package:foresignal_api/src/model/exposure_dependents_by_label.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'exposure_dependents.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ExposureDependents {
  /// Returns a new [ExposureDependents] instance.
  ExposureDependents({

    required  this.status,

     this.attribution,

     this.buildId,

     this.byLabel,

     this.byType,
  });

  @JsonKey(
    
    name: r'status',
    required: true,
    includeIfNull: false,
  )


  final ExposureDependentsStatusEnum status;



  @JsonKey(
    
    name: r'attribution',
    required: false,
    includeIfNull: false,
  )


  final List<Attribution>? attribution;



      /// ULID identifier
  @JsonKey(
    
    name: r'buildId',
    required: false,
    includeIfNull: false,
  )


  final String? buildId;



  @JsonKey(
    
    name: r'byLabel',
    required: false,
    includeIfNull: false,
  )


  final ExposureDependentsByLabel? byLabel;



  @JsonKey(
    
    name: r'byType',
    required: false,
    includeIfNull: false,
  )


  final Map<String, int>? byType;





    @override
    bool operator ==(Object other) => identical(this, other) || other is ExposureDependents &&
      other.status == status &&
      other.attribution == attribution &&
      other.buildId == buildId &&
      other.byLabel == byLabel &&
      other.byType == byType;

    @override
    int get hashCode =>
        status.hashCode +
        attribution.hashCode +
        (buildId == null ? 0 : buildId.hashCode) +
        byLabel.hashCode +
        byType.hashCode;

  factory ExposureDependents.fromJson(Map<String, dynamic> json) => _$ExposureDependentsFromJson(json);

  Map<String, dynamic> toJson() => _$ExposureDependentsToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum ExposureDependentsStatusEnum {
@JsonValue(r'ok')
ok(r'ok'),
@JsonValue(r'unavailable')
unavailable(r'unavailable'),
@JsonValue(r'not-installed')
notInstalled(r'not-installed');

const ExposureDependentsStatusEnum(this.value);

final String value;

@override
String toString() => value;
}


