//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'event_stream_filter_external.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class EventStreamFilterExternal {
  /// Returns a new [EventStreamFilterExternal] instance.
  EventStreamFilterExternal({

     this.sourceIds,
  });

      /// Only these providers
  @JsonKey(
    
    name: r'sourceIds',
    required: false,
    includeIfNull: false,
  )


  final List<String>? sourceIds;





    @override
    bool operator ==(Object other) => identical(this, other) || other is EventStreamFilterExternal &&
      other.sourceIds == sourceIds;

    @override
    int get hashCode =>
        sourceIds.hashCode;

  factory EventStreamFilterExternal.fromJson(Map<String, dynamic> json) => _$EventStreamFilterExternalFromJson(json);

  Map<String, dynamic> toJson() => _$EventStreamFilterExternalToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

