//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/investigation.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'investigation_export.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class InvestigationExport {
  /// Returns a new [InvestigationExport] instance.
  InvestigationExport({

    required  this.header,

    required  this.generatedAt,

    required  this.investigation,

    required  this.attributions,
  });

  @JsonKey(
    
    name: r'header',
    required: true,
    includeIfNull: false,
  )


  final InvestigationExportHeaderEnum header;



  @JsonKey(
    
    name: r'generatedAt',
    required: true,
    includeIfNull: false,
  )


  final DateTime generatedAt;



  @JsonKey(
    
    name: r'investigation',
    required: true,
    includeIfNull: false,
  )


  final Investigation investigation;



  @JsonKey(
    
    name: r'attributions',
    required: true,
    includeIfNull: false,
  )


  final List<String> attributions;





    @override
    bool operator ==(Object other) => identical(this, other) || other is InvestigationExport &&
      other.header == header &&
      other.generatedAt == generatedAt &&
      other.investigation == investigation &&
      other.attributions == attributions;

    @override
    int get hashCode =>
        header.hashCode +
        generatedAt.hashCode +
        investigation.hashCode +
        attributions.hashCode;

  factory InvestigationExport.fromJson(Map<String, dynamic> json) => _$InvestigationExportFromJson(json);

  Map<String, dynamic> toJson() => _$InvestigationExportToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum InvestigationExportHeaderEnum {
@JsonValue(r'Automated analysis inputs; verify before acting')
automatedAnalysisInputsSemicolonVerifyBeforeActing(r'Automated analysis inputs; verify before acting');

const InvestigationExportHeaderEnum(this.value);

final String value;

@override
String toString() => value;
}


