//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/cpa_batch_request_filter.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'cpa_batch_request.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class CpaBatchRequest {
  /// Returns a new [CpaBatchRequest] instance.
  CpaBatchRequest({

    required  this.subject,

    required  this.filter,

     this.horizon = '2h',

     this.maxDcpaM,

     this.top = 20,
  });

      /// `{sourceId}/{externalId}`
  @JsonKey(
    
    name: r'subject',
    required: true,
    includeIfNull: false,
  )


  final String subject;



  @JsonKey(
    
    name: r'filter',
    required: true,
    includeIfNull: false,
  )


  final CpaBatchRequestFilter filter;



      /// CPA horizon. ISO-8601 duration (P7D, PT15M) or shorthand (7d, 15m, 12h).
  @JsonKey(
    defaultValue: '2h',
    name: r'horizon',
    required: false,
    includeIfNull: false,
  )


  final String? horizon;



          // minimum: 0
  @JsonKey(
    
    name: r'maxDcpaM',
    required: false,
    includeIfNull: false,
  )


  final num? maxDcpaM;



          // minimum: 1
          // maximum: 500
  @JsonKey(
    defaultValue: 20,
    name: r'top',
    required: false,
    includeIfNull: false,
  )


  final int? top;





    @override
    bool operator ==(Object other) => identical(this, other) || other is CpaBatchRequest &&
      other.subject == subject &&
      other.filter == filter &&
      other.horizon == horizon &&
      other.maxDcpaM == maxDcpaM &&
      other.top == top;

    @override
    int get hashCode =>
        subject.hashCode +
        filter.hashCode +
        horizon.hashCode +
        maxDcpaM.hashCode +
        top.hashCode;

  factory CpaBatchRequest.fromJson(Map<String, dynamic> json) => _$CpaBatchRequestFromJson(json);

  Map<String, dynamic> toJson() => _$CpaBatchRequestToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

