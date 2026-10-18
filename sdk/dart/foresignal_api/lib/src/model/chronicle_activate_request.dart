//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/chronicle_activate_request_backfill.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'chronicle_activate_request.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ChronicleActivateRequest {
  /// Returns a new [ChronicleActivateRequest] instance.
  ChronicleActivateRequest({

     this.backfill,
  });

  @JsonKey(
    
    name: r'backfill',
    required: false,
    includeIfNull: false,
  )


  final ChronicleActivateRequestBackfill? backfill;





    @override
    bool operator ==(Object other) => identical(this, other) || other is ChronicleActivateRequest &&
      other.backfill == backfill;

    @override
    int get hashCode =>
        (backfill == null ? 0 : backfill.hashCode);

  factory ChronicleActivateRequest.fromJson(Map<String, dynamic> json) => _$ChronicleActivateRequestFromJson(json);

  Map<String, dynamic> toJson() => _$ChronicleActivateRequestToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

