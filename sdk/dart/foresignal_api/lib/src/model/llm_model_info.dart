//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'llm_model_info.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class LlmModelInfo {
  /// Returns a new [LlmModelInfo] instance.
  LlmModelInfo({

    required  this.id,

     this.displayName,

     this.contextWindow,

     this.supportsTools,

     this.supportsEmbeddings,
  });

  @JsonKey(
    
    name: r'id',
    required: true,
    includeIfNull: false,
  )


  final String id;



  @JsonKey(
    
    name: r'displayName',
    required: false,
    includeIfNull: false,
  )


  final String? displayName;



  @JsonKey(
    
    name: r'contextWindow',
    required: false,
    includeIfNull: false,
  )


  final int? contextWindow;



  @JsonKey(
    
    name: r'supportsTools',
    required: false,
    includeIfNull: false,
  )


  final bool? supportsTools;



  @JsonKey(
    
    name: r'supportsEmbeddings',
    required: false,
    includeIfNull: false,
  )


  final bool? supportsEmbeddings;





    @override
    bool operator ==(Object other) => identical(this, other) || other is LlmModelInfo &&
      other.id == id &&
      other.displayName == displayName &&
      other.contextWindow == contextWindow &&
      other.supportsTools == supportsTools &&
      other.supportsEmbeddings == supportsEmbeddings;

    @override
    int get hashCode =>
        id.hashCode +
        (displayName == null ? 0 : displayName.hashCode) +
        (contextWindow == null ? 0 : contextWindow.hashCode) +
        (supportsTools == null ? 0 : supportsTools.hashCode) +
        (supportsEmbeddings == null ? 0 : supportsEmbeddings.hashCode);

  factory LlmModelInfo.fromJson(Map<String, dynamic> json) => _$LlmModelInfoFromJson(json);

  Map<String, dynamic> toJson() => _$LlmModelInfoToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

