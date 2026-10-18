//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'event_stream_filter_chronicle.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class EventStreamFilterChronicle {
  /// Returns a new [EventStreamFilterChronicle] instance.
  EventStreamFilterChronicle({

     this.chronicleIds,
  });

  @JsonKey(
    
    name: r'chronicleIds',
    required: false,
    includeIfNull: false,
  )


  final List<String>? chronicleIds;





    @override
    bool operator ==(Object other) => identical(this, other) || other is EventStreamFilterChronicle &&
      other.chronicleIds == chronicleIds;

    @override
    int get hashCode =>
        chronicleIds.hashCode;

  factory EventStreamFilterChronicle.fromJson(Map<String, dynamic> json) => _$EventStreamFilterChronicleFromJson(json);

  Map<String, dynamic> toJson() => _$EventStreamFilterChronicleToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

