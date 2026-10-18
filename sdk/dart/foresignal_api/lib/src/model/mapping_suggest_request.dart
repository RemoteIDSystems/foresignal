//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/mapping_sample.dart';
import 'package:foresignal_api/src/model/mapping_suggest_request_hints.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'mapping_suggest_request.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class MappingSuggestRequest {
  /// Returns a new [MappingSuggestRequest] instance.
  MappingSuggestRequest({

    required  this.sample,

     this.hints,

     this.confirmEgress = false,
  });

  @JsonKey(
    
    name: r'sample',
    required: true,
    includeIfNull: false,
  )


  final MappingSample sample;



  @JsonKey(
    
    name: r'hints',
    required: false,
    includeIfNull: false,
  )


  final MappingSuggestRequestHints? hints;



      /// Required true when the chat provider is remote (035 classification)
  @JsonKey(
    defaultValue: false,
    name: r'confirmEgress',
    required: false,
    includeIfNull: false,
  )


  final bool? confirmEgress;





    @override
    bool operator ==(Object other) => identical(this, other) || other is MappingSuggestRequest &&
      other.sample == sample &&
      other.hints == hints &&
      other.confirmEgress == confirmEgress;

    @override
    int get hashCode =>
        sample.hashCode +
        hints.hashCode +
        confirmEgress.hashCode;

  factory MappingSuggestRequest.fromJson(Map<String, dynamic> json) => _$MappingSuggestRequestFromJson(json);

  Map<String, dynamic> toJson() => _$MappingSuggestRequestToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

