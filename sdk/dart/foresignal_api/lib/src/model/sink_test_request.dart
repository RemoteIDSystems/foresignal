//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'sink_test_request.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class SinkTestRequest {
  /// Returns a new [SinkTestRequest] instance.
  SinkTestRequest({

     this.kind,

     this.timeoutSeconds = 10,
  });

      /// Kind of the synthetic record to send (default: the first kind in the sink filter, else a point)
  @JsonKey(
    
    name: r'kind',
    required: false,
    includeIfNull: false,
  )


  final String? kind;



          // minimum: 1
          // maximum: 60
  @JsonKey(
    defaultValue: 10,
    name: r'timeoutSeconds',
    required: false,
    includeIfNull: false,
  )


  final int? timeoutSeconds;





    @override
    bool operator ==(Object other) => identical(this, other) || other is SinkTestRequest &&
      other.kind == kind &&
      other.timeoutSeconds == timeoutSeconds;

    @override
    int get hashCode =>
        (kind == null ? 0 : kind.hashCode) +
        timeoutSeconds.hashCode;

  factory SinkTestRequest.fromJson(Map<String, dynamic> json) => _$SinkTestRequestFromJson(json);

  Map<String, dynamic> toJson() => _$SinkTestRequestToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

