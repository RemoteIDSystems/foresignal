//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'ask_progress_event.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class AskProgressEvent {
  /// Returns a new [AskProgressEvent] instance.
  AskProgressEvent({

    required  this.tool,

    required  this.status,

     this.summary,
  });

  @JsonKey(
    
    name: r'tool',
    required: true,
    includeIfNull: false,
  )


  final String tool;



  @JsonKey(
    
    name: r'status',
    required: true,
    includeIfNull: false,
  )


  final AskProgressEventStatusEnum status;



  @JsonKey(
    
    name: r'summary',
    required: false,
    includeIfNull: false,
  )


  final String? summary;





    @override
    bool operator ==(Object other) => identical(this, other) || other is AskProgressEvent &&
      other.tool == tool &&
      other.status == status &&
      other.summary == summary;

    @override
    int get hashCode =>
        tool.hashCode +
        status.hashCode +
        (summary == null ? 0 : summary.hashCode);

  factory AskProgressEvent.fromJson(Map<String, dynamic> json) => _$AskProgressEventFromJson(json);

  Map<String, dynamic> toJson() => _$AskProgressEventToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum AskProgressEventStatusEnum {
@JsonValue(r'calling')
calling(r'calling'),
@JsonValue(r'done')
done(r'done');

const AskProgressEventStatusEnum(this.value);

final String value;

@override
String toString() => value;
}


