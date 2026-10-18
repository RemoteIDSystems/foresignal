//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/read_restriction.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'feed_key_input.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class FeedKeyInput {
  /// Returns a new [FeedKeyInput] instance.
  FeedKeyInput({

    required  this.name,

    required  this.prefixes,

    required  this.restrictions,

    required  this.expiresAt,

     this.privateAccess = false,
  });

  @JsonKey(
    
    name: r'name',
    required: true,
    includeIfNull: false,
  )


  final String name;



      /// Allowed canonical path prefixes (GET/HEAD only)
  @JsonKey(
    
    name: r'prefixes',
    required: true,
    includeIfNull: false,
  )


  final List<String> prefixes;



      /// Mandatory: at least one of sources, kinds, area or bbox; `endpoints` is refused (the prefixes are the endpoint restriction) and private sources are always excluded
  @JsonKey(
    
    name: r'restrictions',
    required: true,
    includeIfNull: false,
  )


  final ReadRestriction restrictions;



      /// ≤ 365 days ahead
  @JsonKey(
    
    name: r'expiresAt',
    required: true,
    includeIfNull: false,
  )


  final DateTime expiresAt;



      /// May read private sources (039)
  @JsonKey(
    defaultValue: false,
    name: r'privateAccess',
    required: false,
    includeIfNull: false,
  )


  final bool? privateAccess;





    @override
    bool operator ==(Object other) => identical(this, other) || other is FeedKeyInput &&
      other.name == name &&
      other.prefixes == prefixes &&
      other.restrictions == restrictions &&
      other.expiresAt == expiresAt &&
      other.privateAccess == privateAccess;

    @override
    int get hashCode =>
        name.hashCode +
        prefixes.hashCode +
        restrictions.hashCode +
        expiresAt.hashCode +
        privateAccess.hashCode;

  factory FeedKeyInput.fromJson(Map<String, dynamic> json) => _$FeedKeyInputFromJson(json);

  Map<String, dynamic> toJson() => _$FeedKeyInputToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

