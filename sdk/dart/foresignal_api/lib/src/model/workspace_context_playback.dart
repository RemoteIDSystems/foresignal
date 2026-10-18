//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'workspace_context_playback.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class WorkspaceContextPlayback {
  /// Returns a new [WorkspaceContextPlayback] instance.
  WorkspaceContextPlayback({

    required  this.state,

     this.rate,
  });

  @JsonKey(
    
    name: r'state',
    required: true,
    includeIfNull: false,
  )


  final WorkspaceContextPlaybackStateEnum state;



          // minimum: 0
  @JsonKey(
    
    name: r'rate',
    required: false,
    includeIfNull: false,
  )


  final num? rate;





    @override
    bool operator ==(Object other) => identical(this, other) || other is WorkspaceContextPlayback &&
      other.state == state &&
      other.rate == rate;

    @override
    int get hashCode =>
        state.hashCode +
        rate.hashCode;

  factory WorkspaceContextPlayback.fromJson(Map<String, dynamic> json) => _$WorkspaceContextPlaybackFromJson(json);

  Map<String, dynamic> toJson() => _$WorkspaceContextPlaybackToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum WorkspaceContextPlaybackStateEnum {
@JsonValue(r'playing')
playing(r'playing'),
@JsonValue(r'paused')
paused(r'paused');

const WorkspaceContextPlaybackStateEnum(this.value);

final String value;

@override
String toString() => value;
}


