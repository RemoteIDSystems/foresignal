//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'trip_check_request.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class TripCheckRequest {
  /// Returns a new [TripCheckRequest] instance.
  TripCheckRequest({

     this.dryRun = false,

     this.occurrence,
  });

      /// Return the briefing without delivering
  @JsonKey(
    defaultValue: false,
    name: r'dryRun',
    required: false,
    includeIfNull: false,
  )


  final bool? dryRun;



      /// Occurrence departure time (default the next occurrence)
  @JsonKey(
    
    name: r'occurrence',
    required: false,
    includeIfNull: false,
  )


  final DateTime? occurrence;





    @override
    bool operator ==(Object other) => identical(this, other) || other is TripCheckRequest &&
      other.dryRun == dryRun &&
      other.occurrence == occurrence;

    @override
    int get hashCode =>
        dryRun.hashCode +
        (occurrence == null ? 0 : occurrence.hashCode);

  factory TripCheckRequest.fromJson(Map<String, dynamic> json) => _$TripCheckRequestFromJson(json);

  Map<String, dynamic> toJson() => _$TripCheckRequestToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

