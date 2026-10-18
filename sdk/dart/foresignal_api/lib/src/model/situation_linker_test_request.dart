//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'situation_linker_test_request.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class SituationLinkerTestRequest {
  /// Returns a new [SituationLinkerTestRequest] instance.
  SituationLinkerTestRequest({

    required  this.fromSeq,

    required  this.toSeq,
  });

  @JsonKey(
    
    name: r'fromSeq',
    required: true,
    includeIfNull: false,
  )


  final int fromSeq;



  @JsonKey(
    
    name: r'toSeq',
    required: true,
    includeIfNull: false,
  )


  final int toSeq;





    @override
    bool operator ==(Object other) => identical(this, other) || other is SituationLinkerTestRequest &&
      other.fromSeq == fromSeq &&
      other.toSeq == toSeq;

    @override
    int get hashCode =>
        fromSeq.hashCode +
        toSeq.hashCode;

  factory SituationLinkerTestRequest.fromJson(Map<String, dynamic> json) => _$SituationLinkerTestRequestFromJson(json);

  Map<String, dynamic> toJson() => _$SituationLinkerTestRequestToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

