//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'briefing_input_audio.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class BriefingInputAudio {
  /// Returns a new [BriefingInputAudio] instance.
  BriefingInputAudio({

    required  this.voice,

     this.format,
  });

      /// The provider's voice, for example `alloy` (OpenAI) or `af_bella` (Kokoro)
  @JsonKey(
    
    name: r'voice',
    required: true,
    includeIfNull: false,
  )


  final String voice;



  @JsonKey(
    
    name: r'format',
    required: false,
    includeIfNull: false,
  )


  final BriefingInputAudioFormatEnum? format;





    @override
    bool operator ==(Object other) => identical(this, other) || other is BriefingInputAudio &&
      other.voice == voice &&
      other.format == format;

    @override
    int get hashCode =>
        voice.hashCode +
        format.hashCode;

  factory BriefingInputAudio.fromJson(Map<String, dynamic> json) => _$BriefingInputAudioFromJson(json);

  Map<String, dynamic> toJson() => _$BriefingInputAudioToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum BriefingInputAudioFormatEnum {
@JsonValue(r'mp3')
mp3(r'mp3'),
@JsonValue(r'opus')
opus(r'opus');

const BriefingInputAudioFormatEnum(this.value);

final String value;

@override
String toString() => value;
}


