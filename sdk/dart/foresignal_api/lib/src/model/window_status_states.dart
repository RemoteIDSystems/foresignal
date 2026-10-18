//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'window_status_states.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class WindowStatusStates {
  /// Returns a new [WindowStatusStates] instance.
  WindowStatusStates({

     this.active,

     this.clear,

     this.unknown,

     this.learning,
  });

  @JsonKey(
    
    name: r'active',
    required: false,
    includeIfNull: false,
  )


  final int? active;



  @JsonKey(
    
    name: r'clear',
    required: false,
    includeIfNull: false,
  )


  final int? clear;



  @JsonKey(
    
    name: r'unknown',
    required: false,
    includeIfNull: false,
  )


  final int? unknown;



  @JsonKey(
    
    name: r'learning',
    required: false,
    includeIfNull: false,
  )


  final int? learning;





    @override
    bool operator ==(Object other) => identical(this, other) || other is WindowStatusStates &&
      other.active == active &&
      other.clear == clear &&
      other.unknown == unknown &&
      other.learning == learning;

    @override
    int get hashCode =>
        active.hashCode +
        clear.hashCode +
        unknown.hashCode +
        learning.hashCode;

  factory WindowStatusStates.fromJson(Map<String, dynamic> json) => _$WindowStatusStatesFromJson(json);

  Map<String, dynamic> toJson() => _$WindowStatusStatesToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

