//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'system_info_llm.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class SystemInfoLlm {
  /// Returns a new [SystemInfoLlm] instance.
  SystemInfoLlm({

     this.configured,

     this.chatProvider,

     this.embeddingProvider,

     this.tokensToday,
  });

  @JsonKey(
    
    name: r'configured',
    required: false,
    includeIfNull: false,
  )


  final bool? configured;



  @JsonKey(
    
    name: r'chatProvider',
    required: false,
    includeIfNull: false,
  )


  final String? chatProvider;



  @JsonKey(
    
    name: r'embeddingProvider',
    required: false,
    includeIfNull: false,
  )


  final String? embeddingProvider;



  @JsonKey(
    
    name: r'tokensToday',
    required: false,
    includeIfNull: false,
  )


  final int? tokensToday;





    @override
    bool operator ==(Object other) => identical(this, other) || other is SystemInfoLlm &&
      other.configured == configured &&
      other.chatProvider == chatProvider &&
      other.embeddingProvider == embeddingProvider &&
      other.tokensToday == tokensToday;

    @override
    int get hashCode =>
        configured.hashCode +
        (chatProvider == null ? 0 : chatProvider.hashCode) +
        (embeddingProvider == null ? 0 : embeddingProvider.hashCode) +
        tokensToday.hashCode;

  factory SystemInfoLlm.fromJson(Map<String, dynamic> json) => _$SystemInfoLlmFromJson(json);

  Map<String, dynamic> toJson() => _$SystemInfoLlmToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

