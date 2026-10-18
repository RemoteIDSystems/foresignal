//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'dossier_sections_overview_links.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class DossierSectionsOverviewLinks {
  /// Returns a new [DossierSectionsOverviewLinks] instance.
  DossierSectionsOverviewLinks({

     this.full,
  });

  @JsonKey(
    
    name: r'full',
    required: false,
    includeIfNull: false,
  )


  final String? full;





    @override
    bool operator ==(Object other) => identical(this, other) || other is DossierSectionsOverviewLinks &&
      other.full == full;

    @override
    int get hashCode =>
        full.hashCode;

  factory DossierSectionsOverviewLinks.fromJson(Map<String, dynamic> json) => _$DossierSectionsOverviewLinksFromJson(json);

  Map<String, dynamic> toJson() => _$DossierSectionsOverviewLinksToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

