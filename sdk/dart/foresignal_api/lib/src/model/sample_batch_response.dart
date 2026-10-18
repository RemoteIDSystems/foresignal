//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/sample_response.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'sample_batch_response.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class SampleBatchResponse {
  /// Returns a new [SampleBatchResponse] instance.
  SampleBatchResponse({

    required  this.items,

    required  this.serverTime,
  });

  @JsonKey(
    
    name: r'items',
    required: true,
    includeIfNull: false,
  )


  final List<SampleResponse> items;



  @JsonKey(
    
    name: r'serverTime',
    required: true,
    includeIfNull: false,
  )


  final DateTime serverTime;





    @override
    bool operator ==(Object other) => identical(this, other) || other is SampleBatchResponse &&
      other.items == items &&
      other.serverTime == serverTime;

    @override
    int get hashCode =>
        items.hashCode +
        serverTime.hashCode;

  factory SampleBatchResponse.fromJson(Map<String, dynamic> json) => _$SampleBatchResponseFromJson(json);

  Map<String, dynamic> toJson() => _$SampleBatchResponseToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

