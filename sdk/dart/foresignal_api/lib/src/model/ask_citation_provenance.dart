//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'ask_citation_provenance.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class AskCitationProvenance {
  /// Returns a new [AskCitationProvenance] instance.
  AskCitationProvenance({

    required  this.refs,

     this.reason,
  });

      /// The cross-linked evidence and objects that put the item in scope
  @JsonKey(
    
    name: r'refs',
    required: true,
    includeIfNull: false,
  )


  final List<String> refs;



      /// A one-line \"why included\"
  @JsonKey(
    
    name: r'reason',
    required: false,
    includeIfNull: false,
  )


  final String? reason;





    @override
    bool operator ==(Object other) => identical(this, other) || other is AskCitationProvenance &&
      other.refs == refs &&
      other.reason == reason;

    @override
    int get hashCode =>
        refs.hashCode +
        (reason == null ? 0 : reason.hashCode);

  factory AskCitationProvenance.fromJson(Map<String, dynamic> json) => _$AskCitationProvenanceFromJson(json);

  Map<String, dynamic> toJson() => _$AskCitationProvenanceToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

