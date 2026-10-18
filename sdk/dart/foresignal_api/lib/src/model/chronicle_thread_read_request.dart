//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'chronicle_thread_read_request.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ChronicleThreadReadRequest {
  /// Returns a new [ChronicleThreadReadRequest] instance.
  ChronicleThreadReadRequest({

    required  this.upToId,
  });

  @JsonKey(
    
    name: r'upToId',
    required: true,
    includeIfNull: false,
  )


  final int upToId;





    @override
    bool operator ==(Object other) => identical(this, other) || other is ChronicleThreadReadRequest &&
      other.upToId == upToId;

    @override
    int get hashCode =>
        upToId.hashCode;

  factory ChronicleThreadReadRequest.fromJson(Map<String, dynamic> json) => _$ChronicleThreadReadRequestFromJson(json);

  Map<String, dynamic> toJson() => _$ChronicleThreadReadRequestToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

