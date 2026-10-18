//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'purge_request.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class PurgeRequest {
  /// Returns a new [PurgeRequest] instance.
  PurgeRequest({

     this.purgeAlerts = false,
  });

      /// Delete referencing alerts instead of redacting them
  @JsonKey(
    defaultValue: false,
    name: r'purgeAlerts',
    required: false,
    includeIfNull: false,
  )


  final bool? purgeAlerts;





    @override
    bool operator ==(Object other) => identical(this, other) || other is PurgeRequest &&
      other.purgeAlerts == purgeAlerts;

    @override
    int get hashCode =>
        purgeAlerts.hashCode;

  factory PurgeRequest.fromJson(Map<String, dynamic> json) => _$PurgeRequestFromJson(json);

  Map<String, dynamic> toJson() => _$PurgeRequestToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

