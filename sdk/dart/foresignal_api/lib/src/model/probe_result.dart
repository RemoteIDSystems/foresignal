//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/normalized_record_preview.dart';
import 'package:foresignal_api/src/model/probe_result_requests_inner.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'probe_result.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ProbeResult {
  /// Returns a new [ProbeResult] instance.
  ProbeResult({

    required  this.sourceId,

    required  this.requests,

    required  this.records,

     this.parseError,
  });

  @JsonKey(
    
    name: r'sourceId',
    required: true,
    includeIfNull: false,
  )


  final String sourceId;



  @JsonKey(
    
    name: r'requests',
    required: true,
    includeIfNull: false,
  )


  final List<ProbeResultRequestsInner> requests;



  @JsonKey(
    
    name: r'records',
    required: true,
    includeIfNull: false,
  )


  final List<NormalizedRecordPreview> records;



  @JsonKey(
    
    name: r'parseError',
    required: false,
    includeIfNull: false,
  )


  final String? parseError;





    @override
    bool operator ==(Object other) => identical(this, other) || other is ProbeResult &&
      other.sourceId == sourceId &&
      other.requests == requests &&
      other.records == records &&
      other.parseError == parseError;

    @override
    int get hashCode =>
        sourceId.hashCode +
        requests.hashCode +
        records.hashCode +
        (parseError == null ? 0 : parseError.hashCode);

  factory ProbeResult.fromJson(Map<String, dynamic> json) => _$ProbeResultFromJson(json);

  Map<String, dynamic> toJson() => _$ProbeResultToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

