//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'hello_event.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class HelloEvent {
  /// Returns a new [HelloEvent] instance.
  HelloEvent({

    required  this.streamId,

    required  this.topics,

    required  this.watermarkSeq,

    required  this.filterVersion,

     this.streamKey,
  });

      /// 129 random bits, base64url; bound to the caller's principal
  @JsonKey(
    
    name: r'streamId',
    required: true,
    includeIfNull: false,
  )


  final String streamId;



      /// Subscribed topics
  @JsonKey(
    
    name: r'topics',
    required: true,
    includeIfNull: false,
  )


  final List<String> topics;



  @JsonKey(
    
    name: r'watermarkSeq',
    required: true,
    includeIfNull: false,
  )


  final int watermarkSeq;



      /// Starts at 1; incremented by each applied filter PATCH
          // minimum: 1
  @JsonKey(
    
    name: r'filterVersion',
    required: true,
    includeIfNull: false,
  )


  final int filterVersion;



      /// Anonymous callers only: send as `Foresignal-Stream-Key` with filter PATCHes
  @JsonKey(
    
    name: r'streamKey',
    required: false,
    includeIfNull: false,
  )


  final String? streamKey;





    @override
    bool operator ==(Object other) => identical(this, other) || other is HelloEvent &&
      other.streamId == streamId &&
      other.topics == topics &&
      other.watermarkSeq == watermarkSeq &&
      other.filterVersion == filterVersion &&
      other.streamKey == streamKey;

    @override
    int get hashCode =>
        streamId.hashCode +
        topics.hashCode +
        watermarkSeq.hashCode +
        filterVersion.hashCode +
        (streamKey == null ? 0 : streamKey.hashCode);

  factory HelloEvent.fromJson(Map<String, dynamic> json) => _$HelloEventFromJson(json);

  Map<String, dynamic> toJson() => _$HelloEventToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

