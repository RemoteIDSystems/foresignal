//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'saved_search_matches_event.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class SavedSearchMatchesEvent {
  /// Returns a new [SavedSearchMatchesEvent] instance.
  SavedSearchMatchesEvent({

    required  this.savedSearchId,

    required  this.name,

    required  this.newMatches,

     this.refs,

    required  this.at,
  });

      /// ULID identifier
  @JsonKey(
    
    name: r'savedSearchId',
    required: true,
    includeIfNull: false,
  )


  final String savedSearchId;



  @JsonKey(
    
    name: r'name',
    required: true,
    includeIfNull: false,
  )


  final String name;



  @JsonKey(
    
    name: r'newMatches',
    required: true,
    includeIfNull: false,
  )


  final int newMatches;



  @JsonKey(
    
    name: r'refs',
    required: false,
    includeIfNull: false,
  )


  final List<String>? refs;



  @JsonKey(
    
    name: r'at',
    required: true,
    includeIfNull: false,
  )


  final DateTime at;





    @override
    bool operator ==(Object other) => identical(this, other) || other is SavedSearchMatchesEvent &&
      other.savedSearchId == savedSearchId &&
      other.name == name &&
      other.newMatches == newMatches &&
      other.refs == refs &&
      other.at == at;

    @override
    int get hashCode =>
        savedSearchId.hashCode +
        name.hashCode +
        newMatches.hashCode +
        refs.hashCode +
        at.hashCode;

  factory SavedSearchMatchesEvent.fromJson(Map<String, dynamic> json) => _$SavedSearchMatchesEventFromJson(json);

  Map<String, dynamic> toJson() => _$SavedSearchMatchesEventToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

