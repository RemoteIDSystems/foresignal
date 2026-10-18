//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'edp_document_item_author.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class EdpDocumentItemAuthor {
  /// Returns a new [EdpDocumentItemAuthor] instance.
  EdpDocumentItemAuthor({

     this.handle,

     this.name,

     this.accountId,
  });

  @JsonKey(
    
    name: r'handle',
    required: false,
    includeIfNull: false,
  )


  final String? handle;



  @JsonKey(
    
    name: r'name',
    required: false,
    includeIfNull: false,
  )


  final String? name;



  @JsonKey(
    
    name: r'accountId',
    required: false,
    includeIfNull: false,
  )


  final String? accountId;





    @override
    bool operator ==(Object other) => identical(this, other) || other is EdpDocumentItemAuthor &&
      other.handle == handle &&
      other.name == name &&
      other.accountId == accountId;

    @override
    int get hashCode =>
        (handle == null ? 0 : handle.hashCode) +
        (name == null ? 0 : name.hashCode) +
        (accountId == null ? 0 : accountId.hashCode);

  factory EdpDocumentItemAuthor.fromJson(Map<String, dynamic> json) => _$EdpDocumentItemAuthorFromJson(json);

  Map<String, dynamic> toJson() => _$EdpDocumentItemAuthorToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

